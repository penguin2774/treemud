;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Contains the Nanny fuction, the loop for logins, account creation
;; 
(ns treemud.nanny
  (:use [treemud.server comm util])
  (:require [treemud.account :as account]))

;; contains login prompt and accout creation dialog. (move?)



	      
(defn- create-account-dialog 
  "Input sequence for creating a new account, returns account when succesfull."
  [user name]
  (let [email (query-loop user input "Please enter your email address."
			  (valid-email? input)
			  "Invalid Email.")
	password (ask-password user)]
    (sendln user "Account created!")
    (let [account (account/create name password email)]
      (account/login account)
      account)))



(defn- ask-create-account 
  "Asks wether or not to create an account."
  [user name]
  (and (ask-yn user (format "Create an account named \"%s\"?" name ))
       (create-account-dialog user name)))


(defn nanny 
  "The Nanny input loop, used for logins and account creation. Returns an account if the user logs in, or creates one.
Called by user's socket handler (server/user-socket)."
  [user]
  (loop [delay 2]
    (send-message user "Login> ")
    (if-let [name ((fn [x] 
                     (println (format "'%s'" x))
                     x)
                     (recv-message user))]
      (if (account/exists? name)
	(do (send-message user "Password> ")
	    (if-let [pass (recv-message user)]
	      (if (and (valid-account-name? name)
		       (valid-password? pass))
		(if-let [account (account/authorize name pass)]
		  (if (account/login account)
					; On success, return account
		    account
		    (do (sendln user "Account already logged in!")
			(recur delay)))
					; Otherwise stall for repeatedly longer periods.
		  (do (sendln user "Incorrect Password.")
		      (Thread/sleep (* delay 100))
		      (recur (* delay 2))))
		(do
		  (sendln user "Invalid account or password.")
		  (recur delay)))))
					; No account, create?
	(if (valid-account-name? name)
	  (or (ask-create-account user name)
	      (recur delay))
	  (do (sendln user "Invalid account name.")
	      (recur delay)))))))

	    

	    
  
