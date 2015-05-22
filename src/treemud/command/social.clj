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



;; Holds all the commands for social interactions, (say emote smote ect.)

(ns treemud.command.social
  (:use [treemud.command.parse :only [def-command]])
  (:require [treemud.server.comm :as comm]
	    [treemud.event :as event]
	    [treemud.world.object :as object]
	    [treemud.act.social :as act]
            [clojure.string :as string]))



(defn do-say 
  "Syntax: say [message]
Says something to everyone in the room.
"
  [user cmd args]
  (act/speak @(:character user) args)) ; hook should take care of launching failure events

(event/def-event-handler :speech [ch cause speech]
  (event/tellln "You say '%s'." speech)
  (event/tellln "%s says '%s'." (object/name cause ch) speech))
		
	    

(def-command do-say "say" :rest)

(defn do-emote 
  "Syntax: emote [action]
Performs a gesture or action, which doesn't effect the game, but is none the less considered to have
happen."
  [user cmd args]
  (act/emote @(:character user) [[object/noun-proper-capital :self :viewer] " " args]))

(event/def-event-handler :emote [ch cause emotion]
  (event/tellln (event/event-string-replace emotion ch cause))
  (event/tellln (event/event-string-replace emotion ch cause)))


(def-command do-emote "emote" :rest)




(event/def-event-handler :emote-apon [ch cause victim speech]
  (event/tellln (event/event-string-replace speech ch cause victim))
  (event/tellln (event/event-string-replace speech ch cause victim)))
