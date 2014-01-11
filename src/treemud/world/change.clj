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
