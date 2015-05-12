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
