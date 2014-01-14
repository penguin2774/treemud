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
  (:require [treemud.server.comm :as comm]
            [treemud.world :as world]
            [treemud.account.file :as file]))

(defn do-save
  "Syntax: save
Saves the charicter and all their equipment"
  [user command]
  (file/save-pc (:account user) (:character user) (map world/to-obj (world/contents-set @(:character user) #{})))
  (comm/sendln user "Charicter Saved!"))

(defn do-quit 
  "Syntax: quit
Logs your character out of the mud."
  [user command]
  
  (comm/sendln user "See you around.")
  (comm/disconnect user))


(def-command do-quit "quit")
(def-command do-save "save")
