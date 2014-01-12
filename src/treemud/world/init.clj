;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Functions for initializing objects

(ns treemud.world.init
  (:require [treemud.consts :as consts])
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

(defn init-room 
  "Initializes the room, used to ready it for *the-world*"
  [obj world]
  (merge room-defaults obj))


(defn- create-vname
    "Gets the next unused number, this could be a problem when dealing
with PC's saved inventory, which are not considered"
    ([vname]
       (symbol (str (name vname) "#" (UUID/randomUUID)))))
	  
		 
(defn insert-obj
  "Inserts obj into world at loc. Obj is the maphash for the object, world is the *the-world* hash and 
loc is a vname already in the world. Loc's contents are also updated. 
loc is optional, and objs without a loc are either rooms or used for cloning in resets and the like.
This means loc doesn't need to have obj in its contents to end up with it there.
Must be called in a transaction, and any vnames refrenced in obj's contents or other fields must be inserted in the same transaction to avoid instability."
  ([obj world]
     (alter world assoc (:vname obj) (ref obj)))
  ([obj world loc]
     (let [vnames-done (atom #{})
           loc-ref (@world loc)]
       
       (when-not (@world (:vname obj))
         (assert loc-ref (format "Location %s doesn't exists in the world" loc))
         (alter world assoc (:vname obj) (ref (assoc obj :location loc)))
         (alter loc-ref assoc :contents (conj (@loc-ref :contents) (:vname obj))) ))))

(defn remove-obj 
  "Removes obj from the world. If obj has contents it dumps those contents into either its own location or 'void.trash"
  ([obj world]
     (alter world dissoc (:vname obj))
     (if (:contents obj)
       (let [dump-loc (or (:location obj) 'void.trash)]
         (doseq [cobj obj]
           (alter (@world cobj) assoc :location dump-loc)
           (alter (@world dump-loc) assoc  :contents (conj (@(@world dump-loc) :contents) cobj)))))))
           

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
     obj)))


(defn init-mobile!
  "Creates a new mobile in the world using an existing one as a base."
  [vname world loc]
  (assert (and (@world vname) (= (:type @(@world vname)) :mobile) ))
  (dosync 
   (let [obj (ref (let [mobile (merge mobile-defaults @(@world vname))]
		    (assoc mobile :vname (create-vname vname)
			   :location loc)))
	 loc (@world loc)]
     (assert loc)
     (alter world assoc (:vname @obj) obj)
     (alter loc assoc :contents (conj (:contents @loc) (:vname @obj)))
     obj)))




(defn init-pc 
  "Initializes the PC for the first time. Done at creation."
  [obj world]
  (merge mobile-defaults obj))

