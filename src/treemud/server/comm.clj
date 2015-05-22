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


;; All socket communication functions. 
;; (note: while in event-handlers use event/x functions.)
;;


(ns treemud.server.comm
  (:require [contrib.except :as except])
  (:use treemud.utils.color))
	

(defn pad-return 
  "Pads all end lines with a \\r, which some clients need."
  [s]
  (.replaceAll s "\n(?!\r)" "\n\r"))

(defn send-message
  "Sends a message to the user."
  ([user message]
     (io!
      (doto (:out user)
	(.write (pad-return message))
	(.flush))))
  ([user message & args]
     (send-message user (apply format message args))))

(defn sendln 
  "Sends a message to the user, and appends '\\n\\r'"
  ([user]
     (send-message user "\n\r"))
  ([user message]
     (send-message user (str message "\n\r")))
  ([user message & args]
     (send-message user (apply format (str message "\n\r") args))))

(defn user-command-prompt 
  "Produces the command prompt for a user whos logged in to the world."
  ([user]
  ":treemud> "))

(defn re-command-prompt 
  "Convenience function for resending the command prompt."
  [user]
   (send-message user (user-command-prompt user)))

(defmacro with-reprompting
  "?Not realy used?
makes sure to resend the prompt after body. If your an event handler, it should be automatic."
  [user & body]
  `(let [user# ~user]
     (sendln user#)
     ~@body
     (sendln user#)
     (re-command-prompt user#)))


(defn recv-message 
  "Reads up till the endline from the user, throws a IOException if the user quits out while reading."
  [user]
  (io!
   (let [result (.readLine (:in user))]
      (if (or (Thread/interrupted) (nil? result))
       (except/throwf java.io.IOException "User disconnected.")
       result))))

(defn prompt
  "Sends 'prompt' to user then recvs a line"
  [user prompt]
  (send-message user prompt)
  (recv-message user))

(defn promptln 
  "Sends 'prompt' and an endline then reads a line"
  [user prompt]
  (sendln user prompt)
  (recv-message user))

(defmacro  working-done
  "Handy macro that sends message, then ... then once body finnishes says 'Done'"
  [user message & body]
  `(do 
     (send-message ~user (str ~message "..."))
     (let [result# ~@body]
       (sendln ~user "Done")
       result#)))
      
(defn disconnect 
  "Disconnects the user, after saying 'Goodbye!'"
  ([user]
     (disconnect user nil))
  ([user reason]
     (io! 
      (sendln user (or reason "Goodbye!"))
      (if-not (= (Thread/currentThread) (:thread user))
	(.interrupt (:thread user))
	(do 
	  (.close (:in user))
	  (.close (:out user))
	  (except/throwf java.io.IOException "User disconnected."))))))

