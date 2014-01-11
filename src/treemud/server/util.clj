;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; A handful of utility functions for running nanny, account managment, 
;; and any other handler code.
;;
(ns treemud.server.util
  (:use [treemud.server comm]))

(defn weak-compare 
  "Returns true if s starts with i (case insensitive)."
  [s i]
  (.startsWith (.toLowerCase s) (.toLowerCase i)))

(defmacro query-loop 
  "A packaged query-loop,
Useful for asking a question repeatedly till you get a valid 
answer. Used in the other question-loop functions.
user          : User
input-name    : name for the input var
question      : string of the question.
valid?        : validation predicate (code block)
fail-responce : (optional) message if responce is invalid."
  [user input-name question valid? & [fail-response]]
  (let [user-sym (gensym "user-")]
  `(let [~user-sym ~user]
     (sendln ~user-sym ~question)
     (loop [~input-name (recv-message ~user-sym)] ; note: nil shouldn't be possible.
       (if-let [result# ~valid?]
	 (if (= true result#) ; if result is simply true, return input
	   ~input-name 
	   result#) ; otherwise return result
	 (do ~(if fail-response
		`(sendln ~user-sym ~fail-response)
		`(sendln ~user-sym ~question))
	     (recur (recv-message ~user-sym))))))))



(defn format-list 
  "Returns a string of the items in a comma seperated string, ending with 'and' (last)
items  : sequience of items to seperated
and-or : replacement string for 'and'"
  [items & [and-or]]
  (reduce str (concat (interpose ", " (butlast items)) [" " (or and-or "and") " " (last items)])))


(defn option-loop 
  "Does a query loop, asking for one of the options given, returning the one requested.
user    : connection being questioned.
menu    : a string holding the menu to be givin.
options : a sequence strings listing the options." 
  [user menu options]
  (query-loop user 
	      input
	      menu
	      (some #(if (weak-compare % input)
		       %)
		       options)
	      (format "Please choose %s." (format-list options "or" ))))

(defn ask-yn 
  "Asks a yes or no question to user. Repeates if invalid."
  [user question]
  (boolean (.startsWith "yes" (query-loop user
				 input
				 question 
				 (re-matches #"(?i-)^(?:ye?s?)|(?:no?)" input)
				 "Yes or No."))))


(defn valid-account-name?
  "A regex to figure out if the account name is sain."
  [name]
  (boolean (and (< 4 (count name) 32)
		(re-matches #"^[a-zA-Z0-9]+$" name))))

(defn valid-email? 
  "Checks if the email is split right, and sees if it can connect to the host named."
  [email]
  (if-let [[full user host] (re-matches #"(\S+)@(\S+)" email)]
    (do (println full user host)
	(try (java.net.InetAddress/getByName host)
	     true
	     (catch java.net.UnknownHostException e
	       false)))
    false))

(defn valid-password? 
  "makes sure the password is atleast 5 characters and less then 126."
  [passwd]
  (and (< 5 (count passwd) 126)))

(defn ask-password 
  "Asks the user to enter a password, then confirm it, if failing, repeating from start."
  [user]
  (query-loop user input "Please enter a password."
	      (if (valid-password? input)
		(do (sendln user "Confirm Password")
		    (= (recv-message user) input))
		false)
	      "Invalid password or password didn't match."))