;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; COPYRIGHT © 2010 Nathanael Cunningham, all rights reserved
;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the file LICENSE at the root of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;       the terms of this license.
;;  You must not remove this notice, or any other, from this software.


;; The server file! master of all things server.
;; Contains the server socet, user socket handler function and the master user list (users)
;; as well the root main function.
;; to start the server, use (utils/launch-thread main) (inside this namespace)


(ns treemud.server
  (:use treemud.server.comm)
  (:require [treemud.utils :as utils]
	    [clojure.tools.logging :as log]
            treemud.account.file
	    [treemud 
             [nanny :as nanny]
	     [command :as command]
	     [account :as account]
	     [world :as world]])
  (:import [java.net ServerSocket
	    SocketException]
	   [java.io BufferedReader
	    InputStreamReader
	    BufferedWriter
	    OutputStreamWriter]))


;; Socket handling details are hashed out here





(defonce ^{:doc "Master shutdown switch. Not to be used directly" 
	   :private true}
  shutdown-switch (atom false))
(defonce ^{:doc "Set of all connected users."} 
  users (atom #{}))

 
(defn create-server 
  "creates and returns a server socket on port, will pass the client
  socket to accept-socket on connection"
  [accept-socket port]
    (let [ss (new ServerSocket port)]
      (utils/launch-thread #(when-not (. ss (isClosed))
			      (try (accept-socket (. ss (accept)))
				   (catch SocketException e))
			      (recur)))
      ss))

		     
    
(def ^{:doc "The welcome banner! loaded from banner.txt" :private true}
     banner (.replace (slurp "banner.txt") "\n" "\n\r"))

(defn- welcome 
  "Sends the welcome banner to the newly connected user."
  [user]
  (io!
   (doto (:out user)
     (.write banner)
     (.flush))))



(defn- goodbye 
  "Cleans up disconnecting users by closeing the in/out streams on user.
 note: for disconnecting users, use (server/comm/disconnect user)"
  [user]
  (if (:in user)
       (.close (:in user)))
  (if (:out user)
       (.close (:out user)))
  (dissoc user :in :out))




(defn- user-socket 
  "This is the base user socket handling function. It welcomes, runs the nanny, account manager and finally the command prompt.
also does the connection, and login/out logging, and error logging for non-command-prompt errors."
  [ins outs addr]
  (log/info (str "User connected from " addr "."))
  (let [user {:in ins :out outs :thread (Thread/currentThread) :ansi-color? true}]
    (swap! users conj user)
    (welcome user)
    (try
     (if-let [account (nanny/nanny user)]
       (try
	(log/info (format "Account Login of %s from %s" (:name @account) addr))
	(let [user (assoc user :account account)]
	  (if-let [pc-objects (account/manage user )] ;; takes pc objects from player file.
	    (let [objs (rest pc-objects)
                  user (assoc user :character (ref (first pc-objects)) )
                  pc (:character user)]
	      (try 
                (world/enter user objs)
                (log/info  (format "Character login %s from %s" (:name @pc) addr))
                (command/prompt user)
                (finally
;                  (world/leave user)
                  (log/info  (format "Character logout of %s from %s" (:name @pc) addr))
		)))))
        (finally 
          (account/logout account)
          (log/info  (format "Account logout of %s from %s" (:name @account) addr)))))
     (catch java.io.IOException e
       (log/info (str "User disconnected from " addr".")))
     (catch Exception e
       (log/fatal e (str "User " addr " booted by uncought exception.")))
     (catch Error e
       (log/fatal e (str "User " addr " booted by uncought error.")))
     (catch Throwable e
       (log/fatal e (str "User " addr " booted by uncought throwable.")))
     (finally
      (swap! users disj user)
      (log/info (str "User " addr " dismissed."))
      (goodbye user)))))
      
	   

 
(defn- start-socket-command-loop
  "Launches the user-socket handler on a new thread."
  [s] (utils/launch-thread user-socket
			   (new BufferedReader (new InputStreamReader (. s (getInputStream)))) ; No... No... I think I need a few more wrapper classes
			   (new BufferedWriter (new OutputStreamWriter (. s (getOutputStream)))) ; There we go ;-)
			   (.getRemoteSocketAddress s))) 

(defonce ^{:doc "The Server socket!" :private true} server (ref nil))


(defn shutdown-server!
  "Used to trigger a safe shutdown of the server.
note: not every well tested..."
  [] (reset! shutdown-switch true))

(defn main 
  "The main loop! Call this to start the mud, note: launch on a new thread to keep your REPL"
  []
  (print "Starting Server...")
  (let [nsrv (create-server start-socket-command-loop  13579)]
    (letfn [(shutdown-server []
			     (print "Shutting down server...")
			     (.close @server)
			     (doseq [user @users]
			       (disconnect user "Mud is shutting down NOW! bbl."))
			     (println "Done"))]
      (reset! shutdown-switch false)
      (try 
       (dosync (ref-set server nsrv))
       (println "Done")
       (while (not @shutdown-switch)
					;world tick?
         (Thread/sleep 1000))
       (catch Exception e
         (log/fatal e "Exception cought in server thread!") ))
      (shutdown-server))))

