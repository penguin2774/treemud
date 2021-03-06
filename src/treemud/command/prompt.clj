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



;; Contains the function for the command loop, catching
;; errors and storing them. As well as the function for running
;; named commands.
;;;;
(ns treemud.command.prompt
  (:require [clojure.tools.logging :as log]
	    [treemud.command.parse :as parse]
	    [treemud.server.comm :as comm]))
	


					; August 03 2010, I realy dont want to go to work tommorow.
					; I'd much rather stay here with Kathryn.


(defn run-command 
  "Finds and runs a command based on the given input."
  [user input]
  (if-let [[cmd-fn args] (parse/lookup-command input)]
    (apply cmd-fn user args)
    (comm/sendln user "Huh?")))



(defn command-prompt
  "The command loop, includes error catching and a basic quit mechanic."
  [user]
  (with-local-vars [quit false]
    (loop [command (comm/prompt user (comm/user-command-prompt user))]
      (if command 
	(if (empty? command)
	  (recur (comm/prompt user (comm/user-command-prompt user)))
	  (do 
	  
	    (try
	     (run-command user command)
	     (catch java.io.IOException e
	       (var-set quit true))
	     (catch Exception e
	       (log/error e (format  "An error occured for %s who typed '%s'." (:name @(:character user)) command))
	       (comm/sendln user "The command you just did crashed, where sorry :( (reason: [%s] '%s'" (class e) (.getMessage e)))
	     (catch AssertionError e
	       (log/error e (format "An error occured for %s who typed '%s'." (:name @(:character user)) command))
	       (comm/sendln user "The command you just did crashed, where sorry :( (reason: [%s] '%s'" (class e) (.getMessage e))))
	    (if-not @quit
	      (recur (comm/prompt user (comm/user-command-prompt user))))))))))
