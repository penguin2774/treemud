;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Password hashing and checking functions.

(ns treemud.utils.passwd
  (:use [rielib.utils :only [digest-message]]))




(defn passwd-hash 
  "Hashes the password given (should be salted) with the MD5 algorithm."
  [string]
  (digest-message string  {:algorithm "MD5" :str? true}))


(defn password-matches? 
  "Tests whether a password matches a hash (don't forget the salt)."
  [passwd hash]
  (= (passwd-hash passwd) hash))