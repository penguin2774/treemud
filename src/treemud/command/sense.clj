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



;; Holds all the commands for seeing, smelling, hearing ect.

(ns treemud.command.sense
  (:use [treemud.command.parse :only [def-command]])
  (:require [treemud.server.comm :as comm]
	    [treemud.utils.color :as color]
            [treemud.world :as world]
	    [treemud.act.sense :as act]
	    [treemud.event :as event]
	    [treemud.world.object :as object]))

(defn do-look 
  "
Syntax: look
        look <object>
Looks around the room, or at an object."
  ([user cmd]
     (act/look @(:character user)))
  ([user cmd obj]
     (let [ch @(:character user)]
       (if-let [obj (or  (object/find-in (:location ch) obj ch)
                         (object/find-in ch obj ch))]
         (act/look ch obj)
       (comm/sendln user "You can't find '%s' anywhere." obj)))))

(event/def-event-handler :look-around [obj cause]
  (event/tellln
   (dosync 
    (let [ch obj
	  loc @(world/lookup (:location ch))
	  {objs :obj mobs :mobile} (group-by #(if (= (:type %1) :mobile)
						:mobile
						:obj) (remove #{ch} (object/contents loc)))]
      (str (color/color-str :yellow :bold
			    (:name loc))
	   "\n\r"
	   (color/color-str :yellow :bold "[Exits:" 
			    (apply str (interpose " " (map first
                                                           (:exits loc)))) "]")
	   "\n\r"
	   (color/color-str :grey  (:desc loc))
	   "\n\r"
	   
	   (color/color-str :blue :bold (apply str (map #(str (:long %) "\n\r") objs)))
	   
	   (color/color-str :blue :bold (apply str (map #(str (object/name %1 ch) " is here. \n\r")  mobs)) "\n\r")))))
  nil)





(event/def-event-handler :look-at [ch cause target]
  (event/tellln
   (dosync
    (str (color/color-str :yellow
                          (object/name target ch))
         "\n\r"
         (color/color-str :grey
                          (:long target))
         "\n\r"
         (color/color-str :blue
                          (str "Wearing: N/A"))
         "\n\r")))
  (event/tellln (str (object/name cause ch)
		     " looks at " (if (= ch target)
				    "you"
				    (object/name target ch)) ".")))
			


(def-command do-look "look")
(def-command do-look "look" :object)

(defn do-look-in 
  ([user cmd _ obj]
   (do-look-in user cmd obj))
  ([user cmd obj]
   (let [ch @(:character user)]
     (if-let [obj (or (object/find-in (:location ch) obj ch)
                      (object/find-in ch obj ch))]
       (cond 
         (and (world/item? obj) (object/container? obj))
         (act/look-in ch obj)
         true
         (comm/sendln user "That's not a container.\n\r"))
       (comm/sendln user "You can't find '%s' anywhere.\n\r" obj)))))


(event/def-event-handler :look-in [ch cause target]
  (event/tellln
   (dosync 
    (str (color/color-str :yellow 
                          "Contents:")
         "\n\r  "
         (if (empty?  (:contents target))
           (color/color-str :brown "Nothing.")
           (apply str 
                  (interpose "\n\r  " (map (fn [obj]
                                             (color/color-str :grey
                                                              (object/short obj ch))) (:contents target)))))
         "\n\r")))
  (event/tellln
   (str (object/name cause ch)
        " rummages through " (if (= ch target)
                               (object/his-her cause ch)
                               (object/short target ch)) ".")))


(def-command do-look-in "look" "in" :object)
(def-command do-look-in "examin" :object)
