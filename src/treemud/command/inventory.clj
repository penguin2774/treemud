;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;  Commands for browsing and managing inventory.

(ns treemud.command.inventory
    (:use [treemud.command.parse :only [def-command]]
	  contrib.except)
    (:require [treemud.event :as event]
	      [treemud.server.comm :as comm]
	      [treemud.utils.color :as color]
	      [treemud.act.inventory :as act]
	      [treemud.world.object :as object]))




(defn do-get [user cmd word]
  (let [ch @(:character user)
	obj (object/find-in (:location ch) word ch)]
    (if obj
      (act/get ch obj)
      (comm/sendln user "You can't find '%s' anywhere." word))))

(defn do-drop [user cmd word]
  (let [ch @(:character user)
	obj (object/find-in ch word ch)]
    (if obj
      (act/drop ch 
		obj)
      (comm/sendln user "You don't seem to have '%s'." word))))

(defn do-give [user cmd target nobj]
  (let [ch @(:character user)
	victim (object/find-in (:location ch) target ch)
	obj (object/find-in ch nobj ch)]
    (cond 
     (not victim)
     (comm/sendln user "You don't see '%s' here." target)
     (not obj)
     (comm/sendln user "You don't have '%s'." nobj)
     true
     (act/give ch obj victim))))

(defn do-inventory [user ch]
  (let [ch @(:character user)]
    (act/examin ch)))

(def-command do-get "get" :object)
(def-command do-drop "drop" :object)
(def-command do-give "give" :object :object)
(def-command do-inventory "inventory")



(event/def-event-handler :took [ch cause obj]
  (event/tellln "You get %s." (object/short obj ch))
  (event/tellln "%s picks up %s." (object/name cause ch)
		(object/short obj ch)))

(event/def-event-handler :dropped [ch cause obj]
  (event/tellln "You drop %s." (object/short obj ch))
  (event/tellln "%s drops %s." (object/name cause ch)
		(object/short obj ch)))

(event/def-event-handler :given [ch cause obj other]
  (event/tellln "You give %s %s" (object/name other ch) 
		(object/short obj ch))
  (if (= ch other)
    (event/tellln "%s gives you %s." (object/name cause ch)
		  (object/short obj ch))
    (event/tellln "%s gives %s %s." (object/name cause ch)
		  (object/name other ch)
		  (object/short obj ch))))

(event/def-event-handler :examine-inventory [ch cause]
  (event/tellln 
   (str
    (color/color-str :red :bold "Inventory:\n\r")
    (color/color-str :yello :bold
		     (apply str 
			    (interpose "\n\r" (map #(object/short % ch)
						   (:contents ch)))))
    "\n\r"))
  nil)
    
    
