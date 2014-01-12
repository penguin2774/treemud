;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; :author Nathanael Cunningham				       ;;
;; :doc All the functions used to load/save and manage account ;;
;;      files. Not designed for direct use, rather for use in  ;;
;;      account.clj					       ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(ns treemud.account.file
  (:refer-clojure :exclude [load])
  (:use [clojure.java.io :only [writer reader]]
	clojure.pprint)
  (:require 
   [treemud.world :as world]
   [treemud.world.init :as world.init]))

(defn- pushback-reader [x]
  (java.io.PushbackReader. (reader x)))



(defn load 
  "Loads the account named from its account file.
returns a reference to it."
  [name]
  (with-open [in (pushback-reader (java.io.File. (str "accounts/" name  ".acc")))]
    (let [account (read in)]
      (assert (and (:name account)
		   (:passwd account)
		   (:email account)))
      (ref account))))

(let [account-folder (java.io.File. "accounts/")] ;; ensures the accounts/ folder exists
  (if-not (.exists account-folder)
    (.mkdirs account-folder)))

(defn save 
  "Saves an account, expects a reference"
  [account]
  (with-open [out (writer (doto (java.io.File. (str "accounts/" (:name @account) ".acc"))
                            
                            (.createNewFile)))]
    (pprint @account out))
  account)



(defn- load-pc
  "Does the individual read for loading the pc from the file."
  [file]
  (with-open [in (pushback-reader file)]
    (let [pc (read in )]
      ;; leave :location for world/enter
      pc)))

(defn load-pcs 
  "Loads all PCs owned by account into a {name, ref} hash. Called by the account manager."
  [account]
  (let [pc-dir (java.io.File. (str "accounts/" (:name @account) "/"))]
    (if (.exists pc-dir)
      (reduce (fn [acc file]
		(let [pc (load-pc file)]
		  (assoc acc (:name pc) (ref pc)))) 
	      {}
	      (filter #(.endsWith (.getName %) ".pc") (file-seq pc-dir)))
      {})))

(defn- serialize-pc 
  "Takes contents and replaces vname with full object's hash, and does the same to the object hash if it has 
a contents field recursivly."
  [pc]
  (let [dehashed-objects (atom #{})]
      (letfn [(deref-vnames [item]
                (swap! dehashed-objects conj (:vname item))
                (if (:contents item)
                  (assoc item :contents (set (for  [i (:contents item)]
                                               (do (assert (not (contains? @dehashed-objects i)) (format  "object %s refrenced twice in object %s (second time)" i (:vname item) ))
                                                   (deref-vnames (world/to-obj i))))))
              item))]
    (deref-vnames pc))))

;;(defn- deserialize-pc [pc]
  
  
(defn save-pc
  "Save the pc to his owner's account folder. Creating anything inbetween there as necessary."
  [pc account]
  (assert (:name @pc))
  (assert (:name @account))
  (let [pc-dir (doto (java.io.File. (str "accounts/" (:name @account)))
		 (.mkdirs))
	pc-file (doto (java.io.File. (str "accounts/" (:name @account) "/" (:name @pc) ".pc"))
		  (.createNewFile))]
    (with-open [out (writer pc-file)]
      (pprint @pc :soul out))) ; TODO: :contents needs to be saved properly as item instances do
                                                   ; not presist between mud resets.
  pc)

(defn account-exists? 
  "Returns true if user account exists by checking if the .acc file exists."
  [name]
  (some #(= name (.replace (.getName %) ".acc" ""))
	(filter #(.endsWith (.getName %) ".acc") (file-seq (java.io.File. "accounts/")))))

(defn pc-exists? 
  "Returns true if the pc exists by checking for its .pc file."
  [name]
  (some #(= name (.replace (.getName %) ".pc" ""))
	(filter #(.endsWith (.getName %) ".pc") (file-seq (java.io.File. "accounts/")))))


(defn create-pc 
  "Creats the PC for the first time, called by the account manager."
  [account name data]
  (save-pc (ref (world.init/init-pc (merge data {:type :mobile
						 :name name
						 :vname (symbol (str "pc." (.replace name " " "-")))})
				    @world/*the-world*)) account))
