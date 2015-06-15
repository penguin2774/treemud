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
;; :doc Commands for dealing with consumeable things like food and water.


(ns treemud.command.consume
  (:use [treemud.command.parse :only [def-command]])
  (:require [treemud.server.comm :as comm]
	    [treemud.utils.color :as color]
            [treemud.world :as world]
	    [treemud.act.social :as social]
            [treemud.act.consume :as act]
	    [treemud.event :as event]
	    [treemud.world.object :as object]))

(defn do-eat [user cmd word]
  (let [ch @(:character user)
        obj (object/find-in ch word ch)]
    (cond
      (not obj)
      (comm/sendln user "You don't have '%s'." word)
      (not (:food obj))
      (social/emote ch [[object/noun-proper-capital :self :viewer] " " [:if-viewer "gnaw" "gnaws"] " on " [object/short obj :viewer] "."])
      :else
      (act/eat ch obj))))

(defn do-drink [user cmd word]
  (let [ch @(:character user)
        obj (object/find-in ch word ch)]
    (cond 
      (not obj)
      (comm/sendln user "You don't have '%s'." word)
      (not (:liquid obj))
      (social/emote ch ["You lift " [object/short obj :viewer]  " up to your mouth and try to drink from it..."]
                    [[object/noun-proper-capital :self :viewer] " lifts " [object/short obj :viewer]  " up to " :self.his-her " mouth and tries to drink from it..."])
      (zero? (:volume (:liquid obj)))
      (comm/sendln user "Sadly, %s is empty." (object/short obj ch))
      :else
      (act/drink ch obj))))


(def-command do-eat "eat" :object)
(def-command do-drink "drink" :object)
(def-command do-drink "drink" "from" :object)

(event/def-event-handler :ate [ch cause obj]
  (event/tellln "You eat %s." (object/short obj ch))
  (event/tellln "%s eats %s." (object/noun-proper-capital cause ch) (object/short obj ch)))

(event/def-event-handler :drank [ch cause obj amount]
  (event/tellln "You drink from %s" (object/short obj ch))
  (event/tellln "%s drinks from %s" (object/noun-proper-capital cause ch) (object/short obj ch)))
