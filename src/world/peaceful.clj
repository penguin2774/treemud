;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2015 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; COPYRIGHT © 2015 Nathanael Cunningham, all rights reserved
;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the file LICENSE at the root of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;       the terms of this license.
;;  You must not remove this notice, or any other, from this software.




;; Generic outdoor area
;; Used for deminstarting seeds and 
;; world building.

(ns world.peaceful
  (:use [treemud.seed :only [def-seed]]))



(def-seed grassy-field [{es :entrance-south size :size pos :position :as settings} ]
  (letfn [(get-vname [x y]
            (symbol "world.peaceful" (str "grassy-field-" x "-"y)))]
    (let [[max-x max-y] (or size [10 10])
          [x y] (or pos [0 0])]
      {:type :room
       :vname (symbol "world.peaceful" (str "grassy-field-" x "-"y))
       :name "A grassy field"
       :desc (str  "You see a grassy field expand in all directions before you.")
       :contents (if (and  (= (quot max-x 2) x)
                           (= (quot max-y 2) y))
                   #{'^:sname world.peaceful/oak-tree
                     '^:sname world.peaceful/hollow-stump
                     '^:sname world.peaceful/acorn
                     '^:sname world.peaceful/nest}
                   #{})
       :exits (merge  (if (and  es (= y 0)) {"north" es} {})
                      (if (> x 0) {"west" (get-vname (- x 1) y)})
                      (if (> y 0) {"north" (get-vname x (- y 1))})
                      (if (< (+ x 1) max-x) {"east"  ['^{:sname :exit} world.peaceful/grassy-field (merge settings {:position [(+ x 1) y]})]})
                      
                      (if (< (inc y) max-y) (if (= 0 x) {"south" ['^{:sname :exit} world.peaceful/grassy-field (merge settings {:position [x (+ y 1)]})]} ;; if first of column produce new entry
                                                {"south" (get-vname x (+ y 1)) }))
                      )})))



(def-seed acorn [_]
  {:type :item
   :name "acorn"
   :short "a tiny acorn"
   :long "A tiny acorn sits here."})

(def-seed oak-tree [_] 
  {:type :item
   :name "tree"
   :short "a tall oak tree"
   :immovable true
   :long "A tall oak tree stands here."})


(def-seed hollow-stump [_]
  {:type :item
   :name "hollow stump"
   :short "a hollow stump"
   :long "An old hollow stump rests here."
   :immovable true
   :contents #{'^:sname world.peaceful/acorn}})

(def-seed area [{es :entrance-south}]
  {:type :room
   :vname 'world.peaceful/sky
   :name "Floating in the air"
   :desc "You float high above a grassy field."
   :exits {:down 'world.peaceful/grassy-field}
   :subrooms #{['^:sname world.peaceful/grassy-field {:entrance-south es} {}]}})

(def-seed egg [_]
  {:type :item
   :name "bird egg"
   :short "a bird's egg"
   :long "A tiny egg rests here."
   :food {:calories 100}})

(def-seed nest [_]
  {:type :item
   :name "bird nest"
   :short "a bird's nest"
   :long "A birds nest rests here."
   :contents #{'^:sname world.peaceful/egg}})


(def-seed mug [_]
  {:type :item 
   :name "clay mug"
   :short "a small clay mug"
   :long "A small clay mug sits here."
   :liquid {:volume 400
            :type :water}})
