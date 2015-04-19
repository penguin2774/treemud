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
;; :doc    All the action functions involved with communication, or other non-game actions such as emoting.
(ns treemud.act.social
    (:use contrib.except)
    (:require [treemud.event :as event]
	      [treemud.world :as world]))


(defn speak
  "Causes a charater to say something.
Success:
  (act current-location :speech ch sentence)"
  ([ch speech]
     (throw-if-not (or (world/item? ch) (world/mobile? ch)) IllegalArgumentException "ch must be an item or mobile.")
     (event/act (:location ch) :speech ch speech)))


(defn emote
  "Causes a character to emote something.
Success:
  (act current-location :emote ch sentence)"
  ([ch speech]
     (event/act (:location ch) :emote ch speech)))
       


(defn emote-apon
  "Causes a character to emote something that involes another."
  ([ch victim speech]
   (event/act (:location ch) :emote-apon ch victim speech)))
   
