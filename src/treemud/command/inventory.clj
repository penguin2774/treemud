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


;;  Commands for browsing and managing inventory.

(ns treemud.command.inventory
    (:use [treemud.command.parse :only [def-command]]
	  contrib.except)
    (:require [treemud.event :as event]
	      [treemud.server.comm :as comm]
	      [treemud.utils.color :as color]
	      [treemud.act.inventory :as act]
	      [treemud.world.object :as object]
              [treemud.world :as world]))




(defn do-get 
  ([user cmd word]
   (let [ch @(:character user)
         obj (object/find-in (:location ch) word ch)]
     (if obj
       (act/get ch obj)
       (comm/sendln user "You can't find '%s' anywhere." word))))

  ([user cmd word1 word2]
   (let [ch @(:character user)
         from (or (object/find-in (:location ch) word2 ch)
                 (object/find-in ch word2 ch))
         obj (object/find-in from  word1 ch)]
     (cond 
       (or (not from) (not (world/item? from)))
       (comm/sendln user "You can't find '%s' anywhere." word2)
       (or (not obj) (not (world/item? obj)))
       (comm/sendln user "You don't see '%s' in %s." word1 (object/short from ch))
       :else
       (act/get ch obj from)))))

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
      (or (not victim) (not (world/mobile? victim)))
      (comm/sendln user "You don't see '%s' here." target)
      (not obj)
      (comm/sendln user "You don't have '%s'." nobj)
      true
      (act/give ch obj victim))))

(defn do-put [user cmd nobj target]
  (let [ch @(:character user)
        dest (or (object/find-in ch target ch) ; Try there inventory first
              (object/find-in (:location ch) target ch))  ; Then there location
        obj (object/find-in ch nobj ch)]
    (cond 
      (or (not dest) (not (world/item? dest)))
      (comm/sendln user "You can't find '%s'." target)
      (not (object/container? dest))
      (comm/sendln user "You can't put '%s' in '%s'." nobj target)
      (not obj)
      (comm/sendln user "You don't have '%s'." nobj)
      true
      (act/put ch obj dest))))

(defn do-inventory [user ch]
  (let [ch @(:character user)]
    (act/examin ch)))


(defn do-empty 
  ([user ch target]
   (do-empty user ch target nil))
  ([user ch from to]
   (let [ch @(:character user)
         obj-from (or (object/find-in ch from ch))
         obj-to   (if to
                    (or (object/find-in ch to ch)
                        (object/find-in (:location ch) to ch))
                    (:location ch))]
     (cond 
       (not obj-from)
       (comm/sendln user "You don't have '%s'.\n\r" from)
       (not obj-to)
       (comm/sendln user "You don't see '%s' anywhere.\n\r" to)
       (= obj-from obj-to)
       (comm/sendln user "You can't empty something into itself!\n\r")
       (or (not  (object/container? obj-to))
           (not (object/container? obj-from)))
       (comm/sendln user "That's not a container.\n\r")
       
       :else
       (act/empty ch obj-from obj-to)))))


(def-command do-get "get" :object)
(def-command do-get "get" :object :object)
(def-command do-drop "drop" :object)
(def-command do-give "give" :object :object)
(def-command do-put "put" :object :object)
(def-command do-inventory "inventory")
(def-command do-empty "empty" :object)
(def-command do-empty "empty" :object :object)


(event/def-event-handler :took [ch cause obj]
  (event/tellln "You get %s." (object/short obj ch))
  (event/tellln "%s picks up %s." (object/name cause ch)
		(object/short obj ch)))

(event/def-event-handler :took-from [ch cause obj from]
  (event/tellln "You get %s from %s." (object/short obj ch) (object/short from ch))
  (event/tellln "%s picks up %s from %s." (object/name cause ch)
		(object/short obj ch)
                (object/short from ch)))


(event/def-event-handler :dropped [ch cause obj]
  (event/tellln "You drop %s." (object/short obj ch))
  (event/tellln "%s drops %s." (object/name cause ch)
		(object/short obj ch)))

(event/def-event-handler :given [ch cause obj other]
  (event/tellln "You give %s %s." (object/name other ch) 
		(object/short obj ch))
  (if (= ch other)
    (event/tellln "%s gives you %s." (object/name cause ch)
		  (object/short obj ch))
    (event/tellln "%s gives %s %s." (object/name cause ch)
		  (object/name other ch)
		  (object/short obj ch))))

(event/def-event-handler :placed [ch cause obj target]
  (event/tellln "You put %s in %s." (object/name target ch)
                (object/short obj ch))
  (event/tellln "%s puts %s in %s." (object/name cause ch)
                (object/short obj ch)
                (object/short target ch)))

(event/def-event-handler :examine-inventory [ch cause]
  (event/tellln 
   (str
    (color/color-str :red :bold "Inventory:\n\r")
    (color/color-str :yellow :bold
		     (apply str 
			    (interpose "\n\r" (map #(object/short % ch)
						   (:contents ch)))))
    "\n\r"))
  nil)
    

(event/def-event-handler :emptied [ch cause from to]
  (event/tellln
   (str 
    (color/color-str :yellow 
                     "You empty out the contents of " (object/short from ch)
                     ". \n\r")))
  (event/tellln 
   (str
    (color/color-str :yellow
                     (object/name cause ch) 
                     " dumps out the contents of " 
                     (object/short from ch)
                     ".\n\r"))))    
