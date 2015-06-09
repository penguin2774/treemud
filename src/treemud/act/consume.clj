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



