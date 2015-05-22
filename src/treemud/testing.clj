;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2015 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; COPYRIGHT © 2015 Nathanael Cunningham, all rights reserved
;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the file LICENSE at the root of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;       the terms of this license.
;;  You must not remove this notice, or any other, from this software.

;; Functions for use in the repl during testing as well
;; as required parts of the mud. Intended as a "scratch pad".
;;


(ns treemud.testing
  (:use clojure.pprint)
  (:require [treemud.server :as server]
            [treemud.world :as world]
            [treemud.world.init :as init]
            [treemud.event :as event]
            [treemud.npc :as npc]
            [treemud.tick :as tick]
            [treemud.event :as event]
            [treemud.seed :as seed]
            [treemud.world.change :as change]
            [treemud.consts :as consts]
            [clojure.set :as set]))



(defn reload-world []
  (dosync
   (let [pcs (mapv world/to-obj-ref (world/pcs-logged-in))
         new-world (world/initiate-world)
         diff (apply set/difference (map #(set (keys %)) new-world world/the-world))]
     (merge @world/the-world new-world)
     (doseq [pc pcs]
       (change/location pc (or (:location @pc) consts/default-room))))))

(defmacro safe-obj-print
  "Usefull for printing references without infinitely recursing on there references"
  [& args]

  `(with-bindings {#'*print-level* 8}
     ~@args))
