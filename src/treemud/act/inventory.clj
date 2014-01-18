;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns treemud.act.inventory
  (:use contrib.except)
  (:require [treemud.world :as world]
	    [treemud.world.change :as change]
	    [treemud.event :as event]))



(defn get [ch obj]
  (letfn [(move-obj! [ch obj]
		    (dosync
		     (if (= (:location @obj) (:location @ch))
		       (do (change/location obj ch)
			   [@ch @obj])
		       (throwf RuntimeException "obj is not in the same place as ch"))))]
    (let [[ch obj] (move-obj! (world/to-obj-ref ch)
			      (world/to-obj-ref obj))]
      (event/act (:location ch) :took ch obj))))



(defn drop [ch obj]

  (letfn [(move-obj! [ch obj]
		    (dosync
		     (if (contains? (:contents @ch) (:vname @obj))
		       (let [loc (world/to-obj-ref (:location @ch))]
			 (do (change/location obj loc)
			     [@ch @obj @loc]))
		       (throwf RuntimeException "ch doesn't have obj."))))]
    (let [[ch obj loc] (move-obj! (world/to-obj-ref ch)
				  (world/to-obj-ref obj))]
      (event/act loc :dropped ch obj))))

(defn give [ch obj other]
  (letfn [(move-obj! [ch obj other]
		     (dosync
		      (cond
		       (not (contains? (:contents @ch) (:vname @obj)))
		       (throwf RuntimeException "obj is not in the same place as ch")
		       (not= (:location @ch) (:location @other))
		       (throwf RuntimeException "ch is not in the same location as other")
		       true
		       (do (change/location obj other)
			   [@ch @obj @other]))))]
    (let [[ch obj other] (move-obj! (world/to-obj-ref ch)
				    (world/to-obj-ref obj)
				    (world/to-obj-ref other))]
      (event/act (:location ch) :given ch obj other))))

(defn put [ch obj target]
  (letfn [(move-obj! [ch obj target]
            (dosync
             (cond
              (not (:contents @target))
              (throwf RuntimeException "target is not a container")
              (not (contains? (:contents @ch) (:vname @obj)))
              (throwf RuntimeException "ch doesn't have obj")
              (and (not= (:location @ch) (:location @target))
                  (not (contains? (:contents @ch) (:vname @target))))
              (throwf RuntimeException "ch doesn't have or isn't near target")
              true?
               (do (change/location obj target)
                   [@ch @obj @target]))))]
    (let [[ch obj target] (move-obj! (world/to-obj-ref ch)
                                     (world/to-obj-ref obj)
                                     (world/to-obj-ref target))]
      (event/act (:location ch) :placed ch obj target))))
                                                       



(defn examin [ch]
  (event/act ch :examine-inventory ch))
