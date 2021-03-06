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


;; Functions for initializing objects

(ns treemud.world.init
  (:require [treemud.consts :as consts]
            [clojure.tools.logging :as log ])
  (:import [java.util UUID] ))

(def room-defaults {:name "A generic room"
		       :desc "Clearly this room requires some love, as its missing a description..."
		       :contents #{}
		       :exits {}
		       :resets []})

(def item-defaults  {:name "lump of clay"
		     :short "a small lump of clay"
		     :long "A small lump of clay lays abandon here."
		     :location consts/default-room})


(def mobile-defaults {:name "Larry"
		      :short "an unremarkable human peasant"
		      :long "An unassuming human peasent is here, looking around bewildered."
		      :race :human
		      :sex :male
		      :levels {:commoner 1}
		      :hps 4
		      :base-attack 0
		      :stats {:str 10
			      :dex 10
			      :con 10
			      :int 10
			      :wis 10
			      :cha 10}

		      :contents #{}
		      :ac 10
		      :skills {:spot 4
			       :profession-farming 4}
		      :feats []
		      :location consts/default-room})

(def npc-defaults (merge mobile-defaults
                        {}))

(def pc-defaults (merge mobile-defaults 
                        {}))
(defn init-room 
  "Initializes the room, used to ready it for *the-world*"
  [obj world]
  (merge room-defaults obj))


(defn- create-vname
    "Gets the next unused number, this could be a problem when dealing
with PC's saved inventory, which are not considered"
    ([vname]
       (symbol (str (name vname) "#" (UUID/randomUUID)))))
	  
		 

(defonce initializers (ref {}))




(defmacro def-initializer [name type & fn-desc]
  (assert (contains? #{:mobile :item :pc} type))
  `(dosync
    (alter initializers assoc-in [~type '~(symbol (str *ns*) (str name))] (fn ~name ~@fn-desc))))

           

;; TODO Items in :contents need to be instanced too.



(defn init-item!
  "Creates a new item in the world using an existing one as a base."
  [vname world loc]
  (assert (and (@world vname) (= (:type @(@world vname)) :item)))
  (dosync 
   (let [obj (ref (let [item (merge item-defaults @(@world vname))]
		    (assoc item :vname (create-vname vname)
                                   :location loc)))
	 loc (@world loc)]
     (assert loc)
     (alter world assoc (:vname @obj) obj)
     (alter loc assoc :contents (conj (:contents @loc) (:vname @obj)))
     (ref-set obj (reduce (fn [obj [name f]]
                            (let [result (f obj)]
                              (assert (and result (map? result)) (format "Initializer [%s] didn't return hash map for item [%s]." name (obj :name)))
                              result))
                          @obj
                          (@initializers :item)))
     obj)))



(defn init-npc!
  "Creates a new mobile in the world using an existing one as a base."
  [vname world loc]
  (assert (and (@world vname) (= (:type @(@world vname)) :mobile) ))
  (dosync 
   (let [obj (ref (let [mobile (merge npc-defaults @(@world vname))]
                     (assoc mobile :vname (create-vname vname)
			   :location loc)))
	 loc (@world loc)]
     (assert loc)
     (alter world assoc (:vname @obj) obj)
     (alter loc assoc :contents (conj (:contents @loc) (:vname @obj)))
     (ref-set obj (reduce (fn [obj [name f]]
                            (let [result (f obj)]
                              (assert (and result (map? result)) (format "Initializer [%s] didn't return hash map for mobile [%s]." name (obj :name)))
                              result))
                          @obj
                          (@initializers :mobile)))
     obj)))




(defn init-pc 
  "Initializes the PC for the first time. Done at creation."
  [obj]
  (dosync
   (reduce (fn [obj [name f]]
             (let [result (f obj)]
               (assert (and result (map? result)) (format "Initializer [%s] didn't return hash map for pc [%s]." name (obj :name)))
               result))
           (merge pc-defaults obj)
           (@initializers :pc))))

