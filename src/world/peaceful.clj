(ns world.peaceful
  (:use [treemud.seed :only [def-seed]]))



(def-seed grassy-field [{es :entrance-south}]
  [{:type :room
    :vname 'world.peaceful/grassy-field
    :name "A grassy field"
    :desc "You see a grassy field expand in all directions before you."
    :contents #{'^:sname world.peaceful/oak-tree
                '^:sname world.peaceful/acorn}
    :exits (if es {"north" es} {})}])



(def-seed acorn [_]
  [{:type :item
    :name "acorn"
    :short "a tiny acorn"
    :long "A tiny acorn sits here."}])

(def-seed oak-tree [_] 
  [{:type :item
    :name "tree"
    :short "a tall oak tree"
    :immovable true
    :long "A tall oak tree stands here."}])

;; {:type :item
;;   :vname peaceful.hollow-stump
;;   :name "hollow stump"
;;   :short "a hollow stump"
;;   :long "An old hollow stump rests here."
;;   :contents #{}}
(def-seed area [{es :entrance-south}]
  [{:type :room
    :vname 'world.peaceful/sky
    :name "Floating in the air"
    :desc "You float high above a grassy field."
    :exits {:down 'world.peaceful/grassy-field}
    :subrooms #{['^:sname world.peaceful/grassy-field {} {:entrance-south es}]}}])
