;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2015 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
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
                     '^:sname world.peaceful/acorn}
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

;; {:type :item
;;   :vname peaceful.hollow-stump
;;   :name "hollow stump"
;;   :short "a hollow stump"
;;   :long "An old hollow stump rests here."
;;   :contents #{}}
(def-seed area [{es :entrance-south}]
  {:type :room
   :vname 'world.peaceful/sky
   :name "Floating in the air"
   :desc "You float high above a grassy field."
   :exits {:down 'world.peaceful/grassy-field}
   :subrooms #{['^:sname world.peaceful/grassy-field {:entrance-south es} {}]}})


