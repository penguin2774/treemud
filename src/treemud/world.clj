;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Contains all the world functions, 
;; which take care of all the game's objects

(ns treemud.world
  (:use contrib.except)
  (:require [treemud.world.load-areas :as loader]
	    [treemud.world.init :as init]
	    [treemud.event.soul :as soul]
	    [treemud.server.comm :as comm]
	    clojure.pprint))

;; The world hash should be stored here, as well as functions to manipulate it.


(defn initiate-world
  "Loads all area files in the area directory and readies them for use in the mud."
  []
  (let [library (loader/load-areas)
	world (reduce (fn [new-world [vname obj]]
			(assoc new-world vname (ref obj))) {} library)]
    (reduce (fn [new-world [vname obj]]
	      (dosync
	       (condp = (:type @obj) 
		 :room
		 (ref-set (new-world vname)
			  (init/init-room @obj new-world))
		 :item
		 (ref-set (new-world vname)
			  (merge init/item-defaults @obj))
		 :mobile
		 (ref-set (new-world vname)
			  (merge init/mobile-defaults @obj))
		 (throwf IllegalArgumentException "Not a known type %s for object %s." (:type obj)
			 (:vname @obj)))
	       
	       new-world)) world world)))

(defonce ^{:doc "The master world hash, all game objects are here.
hashed by there vname."} *the-world* (ref (initiate-world)))
(defonce ^{:doc "A set of all pcs logged in."
	   :private true} *pcs* (ref #{}))
(defonce ^{:doc "A hash of all PC's mobile to there user hashes."
	   :private true} *pcs-to-users* (ref {}))

(defn lookup 
  "Returns the ref of x, x can be a vname or a object map"
  [x]
  (cond
   (symbol? x)
   (@*the-world* x)
   (map? x)
   (@*the-world* (:vname x))))

(defn to-obj 
  "Returns the object hash denoted by x,
x can be its vname, ref to the hash or the hash it self."
  [x]
  (cond 
   (symbol? x) 
   (if-let [result @(lookup x)]
     result
     (throw (Exception. (format "No such object '%s' in world." x))))
   (some #(= (:type x) %) [:room :item :mobile])
   x
   (and (instance? clojure.lang.Ref x) (some #(= (x :type) %) [:room :item :mobile]))
   @x
   true
   (throw (Exception. (format "Don't know how to get an object from '%s'" x)))))

(defn to-obj-ref 
  "Returns the object's ref from the world hash.
Used if you need to change object.
x can be its vname, its hash, or its ref"
  [x]
  (cond 
   (symbol? x)
   (lookup x)
   (some #(= (:type x) %) [:room :item :mobile])
   (lookup (:vname x))
   (and (instance? clojure.lang.Ref x) (some #(= (x :type) %) [:room :item :mobile]))
   x
   true
   (throw (Exception. (format "Don't know how to get an object from '%s'" x)))))

(defn get-user
  "Gets the user logged in on mobile, or nil if no ones logged in on it."
  [vname]
  (assert (or (map? vname) (symbol? vname)))
  (*pcs-to-users* (if (map? vname)
		    (:vname vname)
		    vname)))

(defn object?
  "Returns true if the object is an object in the world."
  [obj]
  (and obj (map? obj) (:vname obj) (*the-world* (:vname obj))))

(defn mobile? 
  "Returns true if obj is a mobile (and an object in the world)"
  [obj]
  (and (object? obj) (= (:type obj) :mobile)))

(defn item?
  "Returns true if the obj is a item (and an object in the world)"
  [obj]
  (and (object? obj) (= (:type obj) :item)))

(defn room? 
  "Returns true if the obj is a room (and an object in the world.)"
  [obj]
  (and (object? obj) (= (:type obj) :room)))


(defn world-print 
  "testing function
pretty-prints an object in the world, or the hole world, with a print level of 8 (to keep from recursing to much)"
  ([]
     (with-bindings {#'*print-level* 8}
       (clojure.pprint/pprint @*the-world*)))
  ([vname]
     (with-bindings {#'*print-level* 8}
       (clojure.pprint/pprint (@*the-world* vname)))))



(defmacro safe-obj-print
  "Usefull for printing references without infinitely recursing on there references"
  [& args]

  `(with-bindings {#'*print-level* 8}
     ~@args))






(defn enter 
  "Enters a user (with a valid :character) into the world, called by user's socket managment function after login."
  [user]
  (let [{ch :character} user
	new-loc (@*the-world* (:location @ch))]
    (dosync
     (ref-set new-loc (assoc @new-loc :contents (conj (:contents @new-loc) (:vname @ch))))
     (alter ch assoc :soul soul/pc-soul)
     (ref-set *the-world* (assoc @*the-world* (:vname @ch)
				 ch))
     (ref-set *pcs* (conj @*pcs* ch))
     (commute *pcs-to-users* assoc (:vname @ch) user))))

(defn leave 
  "Removes the character cleany from the world, used by the user's connection managment function. 
for normal dismissing of users call (disconnect user) or (disconnect (:user @ch))"
  [user]
  (let [{ch :character} user
	loc (@*the-world* (:location @ch))]
    (dosync
     (ref-set loc (assoc @loc :contents (disj (:contents @loc) (:vname @ch))))
     (alter ch dissoc :soul)
     (ref-set *the-world* (dissoc @*the-world* (:vname @ch)))
     (ref-set *pcs* (disj @*pcs* ch))
     (commute *pcs-to-users* dissoc (:vname @ch)))))
    
