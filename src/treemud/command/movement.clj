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


;;  :author Nathanael Cunningham
;;  :doc All commands pertaining to movement, room to room.

(ns treemud.command.movement
  (:use [treemud.command.parse :only [def-command]]
	contrib.except)
  (:require [treemud.event :as event]
	    [treemud.act.movement :as act]
	    [treemud.act.sense :as sense]
	    [treemud.world.object :as object] ))


(defn do-move 
  "Syntax: 
\t ne or northeast
\t n  or north
\t nw or northwest
\t w  or west
\t e  or east
\t se or southeast
\t s  or south
\t sw or southwest

Moves your character in that direction."
  [user cmd]
  (let [ch @(:character user)
	dir-dict {"nw" "northwest" "n" "north" "ne" "northeast"
		  "w"  "west"                  "e"  "east"
		  "sw" "southwest" "s" "south" "se" "southeast"}
	dir (if (contains? (set (vals dir-dict)) cmd)
	      cmd
	      (dir-dict cmd))]
    (act/move ch dir)))

(event/def-event-handler :left-room [ch cause dir]
  (event/tellln "You leave %s" dir)
  (event/tellln "%s leaves to the %s" (object/name cause ch) dir))

(event/def-event-handler :entered-room [ch cause dir]
  (sense/look ch)
  (event/tellln "%s enters from the %s" (object/name cause ch) dir))

(event/def-event-handler :cant-move [ch _ cause]
  (condp = cause
    :no-such-exit
    (event/tellln "You can't go that way."))
  (throwf Exception ":cant-move event should only be passed to the causeing mobile."))


(doseq [dir ["nw" "northwest" "n" "north" "ne" "northeast"
	     "w"  "west"                  "e"  "east"
	     "sw" "southwest" "s" "south" "se" "southeast"]]
	(def-command do-move dir))

