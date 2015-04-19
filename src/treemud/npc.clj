;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Contains functions for NPCs 

(ns treemud.npc
  (:require [clojure.tools.logging :as log]))


(def npc-behavior-table (ref {}))

(def npc-pending-actions (ref []))

(defn add-behavior 
  "Adds a behavior to the global table. Symbol should be namespace qualified.
Idealy added with (define-behavior)."
  [sym fn]
  (dosync 
    (alter npc-behavior-table assoc sym fn)))


(defn lookup-behavior [sym]
  (@npc-behavior-table sym))

(defmacro define-behavior [name & fn-def]
  (let [name-ns (symbol (str *ns*) (str name))] ; makes sure name is in local ns
    `(add-behavior '~name-ns
                   (fn ~name ~@fn-def))))


(defn npc-default-behavior 
  "Final call if no behaviors have an event handler.
  Currently does nothing."
  [e self cause & data]
  nil)


(defn npc-soul-multiplexer 
  "Calls all behavior functions on an NPC until one returns true or they are exhausted."
  [e self cause & data]

  (assert (= (:type self) :mobile))
  (if-let [behaviors (:behaviors self)]
    (let [responces (mapcat (fn [behavior]
                              (apply (lookup-behavior behavior) e self cause data)) behaviors)]
      (log/info (str "Behaviors " behaviors))
      (dosync (alter npc-pending-actions  #(vec (concat % responces)))))))


