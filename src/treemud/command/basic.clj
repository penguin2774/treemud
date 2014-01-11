;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; :author Nathanael Cunningham
;; :doc Holds basic commands, essentials like who and quit.
(ns treemud.command.basic
  (:use treemud.command.parse)
  (:require [treemud.server.comm :as comm]))

(defn do-quit 
  "Syntax: quit
Logs your character out of the mud."
  [user command]
  
  (comm/sendln user "See you around.")
  (comm/disconnect user))


(def-command do-quit "quit")