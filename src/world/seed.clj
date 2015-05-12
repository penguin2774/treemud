;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2015 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The root seed that causes all others to
;; seed in.

(ns world.seed
  (:use [treemud.seed :only [def-seed]])
  (:require [world.void :as void]
            [world.peaceful :as peaceful]))



(def-seed the-world-seed [_]
  {:type :item
   :vname 'world.seed/the-world-seed
   :location 'world.peaceful/sky
   :name "world seed"
   :short "a tiny seed"
   :long "a tiny glowing seed sits here."
   :desc "It's a tiny acorn, it glows with soft lights alterinating between blue and green."
   :areas #{['^:sname world.void/area {:entrance-north 'world.peaceful/grassy-field-0-0} {}]
            ['^:sname world.peaceful/area {:entrance-south 'world.void/start} {}]}})
