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
;; :doc All actions which lets mobiles see/smell/hear/ESP ect.

(ns treemud.act.sense
    (:use contrib.except)
    (:require [treemud.event :as event]
	      [treemud.world :as world]))


(defn look
  "Triggers the look-at or look-around events depending on the args.
Success:
  [ch]
  (act loc :look-around ch)
  [ch obj]
  (act loc :look-at ch obj)"
  ([ch]
     (throw-if-not (world/mobile? ch) IllegalArgumentException "ch must be a mobile.")
     (let [loc (world/to-obj (:location ch))]
       (event/act loc :look-around ch)))
  ([ch obj]
     (throw-if-not (world/mobile? ch) IllegalArgumentException "ch must be a mobile.")
     (throw-if-not (world/object? ch) IllegalArgumentException "obj must be an object.")
     (event/act (:location ch) :look-at ch obj)))
       
