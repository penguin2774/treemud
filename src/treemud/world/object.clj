;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Contains all the functions for getting data from an object while taking
;; game computations into consideration.

(ns treemud.world.object
  (:refer-clojure :exclude [name])
  (:use contrib.except)
  (:require [treemud.world :as world]))

;; utility functions for working with objects

(defn contents
  "Gets the contents of an object, optionaly filtered by type."
  ([obj]
     (let [obj (world/to-obj obj)]
       (map #(deref (world/lookup %)) (:contents obj))))
  ([obj type]
     (filter #(= (:type %) type) (contents obj))))

(defn container?
  "Returns whether obj is a container."
  [obj]
  (contains? (world/to-obj obj) :contents))

(defn name
  "Gets the name of an object based on what looker can see."
  [obj looker]
  (let [obj (world/to-obj obj)
	looker (world/to-obj looker)]
    (condp = (:type obj)
      :mobile 
      ;; <greet system goes here>
      (:name obj)
      (:name obj))))

(defn short
  "Gets the objects short description based on what looker can see."
  [obj looker]
  (let [obj (world/to-obj obj)
	looker (world/to-obj looker)]
    (:short obj)))

(defn sex
  "Gets the sex based on what looker can see."
  [obj looker]
  (let [obj (world/to-obj obj)
	looker (world/to-obj looker)]
    (:sex obj)))

(defn noun-proper [person looker]
  (if (= person looker)
    "you"
    (name person looker)))

(defn noun-proper-capital [person looker]
  (if (= person looker)
    "You"
    (clojure.string/replace-first (name person looker)
                                  #"\w+"
                                  clojure.string/capitalize)))


(defn his-her
  "Gets the \"his/her\" string based on what the looker can see."
  [obj looker]
  (let [obj (world/to-obj obj)
	looker (world/to-obj looker)]
    (if (= obj looker)
      "your"
      (case (:sex obj)
        :female "her"
      :male "his"))))


(defn find-in 
  "Finds an object named by input inside obj based on what looker can see.
input is not checked case sensitivly, and the first name which contains input is taken."
  [obj input looker]
  (let [contents (contents obj)]
    (some (fn [x]
	    (if (.contains (.toLowerCase (name x looker)) (.toLowerCase input))
	      x)) contents)))

