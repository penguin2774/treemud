;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; COPYRIGHT © 2010 Nathanael Cunningham, all rights reserved
;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the file LICENSE at the root of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;       the terms of this license.
;;  You must not remove this notice, or any other, from this software.


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;  All the functions used to load/save and manage account     ;;
;;  files. Not designed for direct use, rather for use in      ;;
;;  account.clj					               ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;





(ns treemud.account.file
  (:refer-clojure :exclude [load])
  (:use [clojure.java.io :only [writer reader]]
	clojure.pprint)
  (:require 
   
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
    (let [all-objects
          (loop [acc [] obj (read in nil :eof)]
            (if-not (= obj :eof)
              (recur (conj acc obj) (read in nil :eof))
              acc))]
      (assert (= (:type (first all-objects)) :mobile) (format "First object in PC file %s was not a mobile"
                                                              file))
      ;; leave :location for world/enter
      all-objects)))

(defn load-pcs
  "Loads all PCs owned by account into a {name, [ch items...]} hash. Called by the account manager."
  [account]
  (let [pc-dir (java.io.File. (str "accounts/" (:name @account) "/"))]
    (if (.exists pc-dir)
      (reduce (fn [acc file]
		(let [pc-data (load-pc file)]
		  (assoc acc (:name (first pc-data)) pc-data ))) 
	      {}
	      (filter #(.endsWith (.getName %) ".pc") (file-seq pc-dir)))
      {})))

;; Ganna go with the area file style of serialization, and just simply dump all :contents objects straight into
;; the player file after the pc object.
;; When loading We'll just merge the hole file into world in one transaction. This should work better then manualy
;; packing and unpacking players inventories.



;; (defn- serialize-pc 
;;   "Takes contents and replaces vname with full object's hash, and does the same to the object hash if it has 
;; a contents field recursivly."
;;   [pc]
;;   (let [dehashed-objects (atom #{})]
;;       (letfn [(deref-vnames [item]
;;                 (swap! dehashed-objects conj (:vname item))
;;                 (if (:contents item)
;;                   (assoc item :contents (set (for  [i (:contents item)]
;;                                                (do (assert (not (contains? @dehashed-objects i)) (format  "object %s refrenced twice in object %s (second time)" i (:vname item) ))
;;                                                    (deref-vnames (world/to-obj i))))))
;;               item))]
;;     (deref-vnames pc))))

;;(defn- deserialize-pc [pc]


(defn save-pc
  "Save the pc to his owner's account folder. Creating anything inbetween there as necessary."
  [account pc items]
  (assert (:name pc))
  (assert (:name account))
  
  (let [pc-dir (doto (java.io.File. (str "accounts/" (:name account)))
                 (.mkdirs))
        pc-file (doto (java.io.File. (str "accounts/" (:name account) "/" (:name pc) ".pc"))
                  (.createNewFile))
        objs-to-file (cons (dissoc pc :soul) items) ]
    (assert objs-to-file (first objs-to-file))
    (with-open [out (writer pc-file)]
      (doseq [cobj objs-to-file]
        (pprint cobj out)))
    objs-to-file))

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
  (save-pc @account
           (world.init/init-pc (merge data {:type :mobile
                                            :name name
                                            :vname (symbol (str "pc." (.replace name " " "-")))}))
           []))
