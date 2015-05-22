;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; COPYRIGHT © 2010 Nathanael Cunningham, all rights reserved
;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the file LICENSE at the root of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;       the terms of this license.
;;  You must not remove this notice, or any other, from this software.



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
  (file/save-pc @(:account user) @(:character user) (map world/to-obj (:contents @(:character user))))
  (comm/sendln user "Charicter Saved!"))

(defn do-quit 
  "Syntax: quit
Logs your character out of the mud."
  [user command]
  
  (comm/sendln user "See you around.")
  (comm/disconnect user))


(def-command do-quit "quit")
(def-command do-save "save")
