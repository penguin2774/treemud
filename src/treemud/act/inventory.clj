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



(ns treemud.act.inventory
  (:refer-clojure :exclude [get drop])
  (:use contrib.except)
  (:require [treemud.world :as world]
	    [treemud.world.change :as change]
	    [treemud.event :as event]
            [treemud.world.object :as object]))



(defn get 
  "Moves object obj to ch inventory."
  ([ch obj]
   (letfn [(move-obj! [ch obj]
             (dosync
              (if (= (:location @obj) (:location @ch))
                (do (change/location obj ch)
                    [@ch @obj])
                (throwf RuntimeException "obj is not in the same place as ch"))))]
     (let [[ch obj] (move-obj! (world/to-obj-ref ch)
                               (world/to-obj-ref obj))]
       (event/act (:location ch) :took ch obj))))
  ([ch obj from]
   (letfn [(move-obj! [ch obj from]
             (dosync
              (do (change/location obj ch)
                  [@ch @obj @from])))]
     (let [[ch obj from] (move-obj! (world/to-obj-ref ch)
                                    (world/to-obj-ref obj)
                                    (world/to-obj-ref from))]
       (event/act (:location ch) :took-from ch obj from)))))



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
              :else
               (do (change/location obj target)
                   [@ch @obj @target]))))]
    (let [[ch obj target] (move-obj! (world/to-obj-ref ch)
                                     (world/to-obj-ref obj)
                                     (world/to-obj-ref target))]
      (event/act (:location ch) :placed ch obj target))))
                                                       
(defn empty [ch from to]
  (letfn [(move-obj! [ch from to]
            (dosync
             (cond
              (not (:contents @from))
              (throwf RuntimeException "from is not a container")
              (not (:contents @to))
              (throwf RuntimeException "to is not a container")
              (not (or (= (:location @ch) (:location @from)) ;; next to each other
                       (= (:vname @ch) (:location @from)))) ;; ch has from
              (throwf RuntimeException "ch isn't near from %s %s" (:vname @ch) (:location @from) )
              (not (or (= (:location @ch) (:location @to)) ;; next to each other
                       (= (:vname @ch) (:location @to)) ;; ch has to
                       (= (:location @ch) (:vname @to)))) ;; ch is in to

              (throwf RuntimeException "ch isn't near to")
              :else
              (do (let [objs (object/contents from)]
                    (doseq [obj objs]
                      (change/location obj to))
                    [@ch @from @to])))))]
    (let [[ch obj target] (move-obj! (world/to-obj-ref ch)
                                     (world/to-obj-ref from)
                                     (world/to-obj-ref to))]
      (event/act (:location ch) :emptied ch from to))))


(defn examin [ch]
  (event/act ch :examine-inventory ch))
