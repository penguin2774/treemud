;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT � 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(ns treemud.account
  (:refer-clojure :exclude [load])
  (:use treemud.account.manage
	treemud.account.file)
  (:require [treemud.utils.passwd :as passwd]))


;; holds all functions relating to accounts and the account managment menu.
(def *accounts* (atom #{}))

(defn create 
  "Creates an account, and hashes the password."
  [#^String name #^String password #^String email]
  (save (ref {:name name :passwd (passwd/passwd-hash (str (.substring name 3) ":" password)) :email email
	 :rpoints 0})))

(defn authorize 
  "Tests the password agenst the hashed password in 'name's account file"
  [#^String name #^String passwd]
  (assert (account-exists? name))
  (let [account (load name)]
    (if (passwd/password-matches? (str (.substring name 3) ":" passwd) (:passwd @account))
      account)))

(defn exists? 
  "Returns true if named account exists."
  [#^String name]
  (account-exists? name))


(defn login 
  "Adds account to the set of logged in accounts."
  [account]
  (swap! *accounts* conj account)
  account)

(defn logout 
  "Removes account from the set of logged in accounts."
  [account]
  (swap! *accounts* disj account)
  account)


(defn manage 
  "Runs the account manager for user. Ends in either an IOException
when the user disconnects, or the pc structure the user wants to log
into."
  [user]
  (manage-account user))
