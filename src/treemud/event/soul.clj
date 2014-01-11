;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Contains the 'soul' function for PCs. Which is responsable for calling the appropriate event handler for spesific events.
;;
(ns treemud.event.soul
  (:require [clojure.tools.logging :as log]))

(let [ *pc-event-fns* (atom {})]
  
  (defn pc-soul 
    "The function in the pc's :soul. Calls the appropriate event handler"
    [e self cause & data]
    (if (@*pc-event-fns* e)
      (apply (@*pc-event-fns* e) self cause data)
      (log/warn (format "No known pc-event-handler for '%s' cause by %s." e (:vname self)))))
  
  (defn register-event 
    "Registers an event, called by def-event-handler. (Note: multiple calls overwrites the original"
    [e fn]
    (swap! *pc-event-fns* assoc e fn)))
