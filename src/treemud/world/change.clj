;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; All functions that apply changes to objects in the world

(ns treemud.world.change
  (:use contrib.except)
  (:require [treemud.world :as world]))


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

(defn location
  "Changes the location of obj to new-loc, updating the old location if obj has one.."
  [obj new-loc]
  (assert (:contents @new-loc))
  (if (:location @obj)
    (let [old-loc (world/to-obj-ref (:location @obj))]
      (alter old-loc assoc 
	     :contents (disj (:contents @old-loc) (:vname @obj)))))
  (alter  new-loc assoc 
	  :contents (conj (:contents @new-loc) (:vname @obj)))
  (alter  obj assoc
	  :location (:vname @new-loc)))
