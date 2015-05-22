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



(ns world.void
  (:use [treemud.seed :only [def-seed]]))


(def-seed start  [{en :entrance-north }]
  {:vname 'world.void/start
   :type :room
   :name "A formless void"
   :desc "You float in a formless void."
   :exits (merge  {"east" 'world.void/trash}
                  (if en {"south" en}))})

(def-seed trash [_]
  {:vname 'world.void/trash
   :type :room
   :name "A formless void"
   :desc "You float in a formless void."
   :exits {"west" 'world.void/start}})




(def-seed area [{en :entrance-north}]
  {:vname 'world.void/area
   :type :room
   :name "A formless void"
   :desc "You float in the very center of the void."
   :exits {}
   :subrooms #{['^:sname world.void/start {:entrance-north en}]
               '^:sname world.void/trash}})
