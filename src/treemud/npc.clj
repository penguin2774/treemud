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
  (:require [clojure.tools.logging :as log]
            [rielib.utils :as utils]))


(def ^{:doc "Global behavior table. Holds a ref to all the defined behavior functions and there associated symbol."}
  npc-behavior-table (ref {}))

(def npc-pending-actions ^{:doc "NPC Actions waiting to happen."} (ref []))



(defn add-behavior 
  "Adds a behavior to the global table. Symbol should be namespace qualified.
Idealy added with (define-behavior)."
  [sym fn]
  (dosync 
    (alter npc-behavior-table assoc sym fn)))


(defn lookup-behavior 
  "Looks up a behavior in the behavior table by its symbol, returns it's function."
  [sym]
  (@npc-behavior-table sym))

(defmacro define-behavior 
  "Defines a behavior. Adding it to the global table in a transaction.
Behavior args are always [event self cause & data]
  event - keyword of the event
  self - object the event is happening to
  cause - source of the event
  data - other args related to the event.
All Behavior functions should return a vector of actions to be preformed as a result of the action.

Behavior functions should not modify the world directly but use resulting actions.
"
  [name & fn-def]
  (let [name-ns (symbol (str *ns*) (str name))] ; makes sure name is in local ns
    `(add-behavior '~name-ns
                   (fn ~name ~@fn-def))))





(defn npc-soul-multiplexer 
  "Calls all behavior functions on an NPC until one returns true or they are exhausted.
Placed on mobiles as :soul function. Should not be called directly."
  [e self cause & data]

  (assert (= (:type self) :mobile))
  (if-let [behaviors (:behaviors self)]
    (let [responces (mapcat (fn [behavior]
                              (apply (lookup-behavior behavior) e self cause data)) behaviors)]
      (dosync (alter npc-pending-actions  #(vec (concat % responces)))))))

(defn- process-actions! []
  (letfn [(pull-next-action []
            (dosync (let [result (first @npc-pending-actions)]
                      (alter npc-pending-actions next)
                      result)))]
    (loop [action (pull-next-action)]
      (if action 
        (dosync 
         (let [{f :fn self :mobile args :args} action]
           
           (try 
             (assert (and f self))
             (assert (=  (:type self) :mobile))
             (assert (fn? f))


             (apply f self args)
             (catch Exception e
               (log/error e (format "An error occured when npc '%s' tried '%s'. Args: [%s]" (:vname self) f args)))
             (catch AssertionError e 
               (log/error e (format "Npc '%s' failed an assertion when they tried '%s'. Args: [%s]" (:vname self) f args)))
             (finally 
                      (log/error  "Unknown error with Npc '%s' while attempting to call '%s' args: [%s]" (:name self) f args)))))
        (Thread/sleep 100)) ;; if no actions, sleep.
      (recur (pull-next-action)))))

(defonce npc-process-thread-running (atom false))

(defn launch-npc-process-actions-thread
  "Launches the process actions thread."
  []
  (if (swap! npc-process-thread-running not)
    (utils/launch-thread process-actions!)))



  
