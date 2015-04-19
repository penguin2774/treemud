;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Contains generic behaviors 

(ns treemud.npc.generic
  (:require [treemud.npc :as npc]
            [treemud.world :as world]
            [treemud.server :as server])
  (:import [java.io StringWriter]))



(npc/define-behavior spy [e self cause & data]
  (if-let [master (world/lookup (@self :master))
           master-user (world/get-user master)]
    (if master ; master is logged in. Report all output to them.
      (let [string-catcher (StringWriter.)]
        
        ))))
