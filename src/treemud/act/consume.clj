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



;; :author Nathanael Cunningham
;; :doc Commands for dealing with consumeable things like food and water.

(ns treemud.act.consume
    (:use contrib.except)
    (:require [treemud.event :as event]
              [treemud.world.change :as change]
              [treemud.world.object :as object]
	      [treemud.world :as world]))


(defmacro sanity-cond [& body]
  (if (even?  (count body))
    (throw IllegalArgumentException "Must have odd number of arguments."))
  `(cond ~@(mapcat (fn [[prep arg]] (list`(not ~prep)
                                         (if (string? arg)
                                           `(throwf RuntimeException ~arg)
                                           arg)))
                   (partition 2 (butlast body)))
         :else
         ~(last body)))



(defn eat [ch obj]
  (cond 
    (not (world/item? obj))
    (throwf RuntimeException "obj isn't an item.")
    (not (world/mobile? ch))
    (throwf RuntimeException "ch isn't a mobile.")
    (not (:food obj))
    (throwf RuntimeException "obj isn't a food.")
    :else
    (let [[ch obj] (dosync 
                    (let [ch (world/to-obj-ref ch)
                          obj (world/to-obj-ref obj)]
                      (alter ch update-in [:hunger] (fn [hunger]
                                                      (max (- (or hunger 0) (or (:calories (:food @obj)) 100)) 0)) )
                
                      ;; effect system here

                      (change/remove-obj obj)
                      [@ch @obj]))]
      (event/act (:location ch) :ate ch obj))))

(def volume-per-drink 100) ; ml (about 4 fl oz)

(def liquid-table
  {:water {:hydration 100}})

(def default-liquid :water)

(defn drink [ch obj]
  (sanity-cond
    (world/item? obj)
    "obj isn't an item."
    (world/mobile? ch)
    "ch isn't a mobile."
    (:liquid obj)
    "obj doesn't contain liquid."
    (:volume (:liquid obj))
    "(:liquid obj) doesn't have a :volume."
    (> (:volume (:liquid obj)) 0)
    ":liquid's volume is negitive."
    (let [[ch obj max-vol-drank] 
          (dosync 
           (let [ch (world/to-obj-ref ch)
                 obj (world/to-obj-ref obj)
                 liquid-data (:liquid @obj)
                 max-vol-drank (min (:volume liquid-data) volume-per-drink) ]
             (alter ch update-in [:thirst] (fn [thirst]
                                             (if-let [hydration (:hydration (liquid-table (or  (:name liquid-data) default-liquid)))] 
                                               (max (- (or thirst 0) (* hydration (/ max-vol-drank volume-per-drink))) 0))))
                      ;; effect system here
             
             (alter obj update-in [:liquid :volume] (fn [vol]
                                                      (max (- vol max-vol-drank) 0) ; shouldn't need max...
                                                      ))
             [@ch @obj max-vol-drank]))]
         (event/act (:location ch) :drank ch obj max-vol-drank))))

