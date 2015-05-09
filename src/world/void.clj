(ns world.void
  (:use [treemud.seed :only [def-seed]]))


(def-seed start  [{en :entrance-north }]
  [{:vname 'world.void/start
     :type :room
     :name "A formless void"
     :desc "You float in a formless void."
    :exits (merge  {"east" 'world.void/trash}
                   (if en {"south" en}))}])

(def-seed trash [_]
  [{:vname 'world.void/trash
     :type :room
     :name "A formless void"
     :desc "You float in a formless void."
     :exits {"west" 'world.void/start}}])




(def-seed area [{en :entrance-north}]
  [{:vname 'world.void/area
    :type :room
    :name "A formless void"
    :desc "You float in the very center of the void."
    :exits {}
    :subrooms #{['^:sname world.void/start {} {:entrance-north en}]
                '^:sname world.void/trash}}])
