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
;; :doc Holds the command parser.

(ns treemud.command
    (:require [treemud.command basic
	       sense movement social inventory]
	      treemud.server.comm
	      treemud.command.prompt))

;; The command parser goes here



(defn prompt [user]
  (treemud.command.prompt/command-prompt user))
