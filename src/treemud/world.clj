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
  (:require clojure.set
            [treemud.world.load-areas :as loader]
	    [treemud.world.init :as init]
	    [treemud.event.soul :as soul]
	    [treemud.utils.hooks :as hooks]
            [treemud.account.file :as pc-file]
            [treemud.seed :as seed]
            world.seed
	    clojure.pprint
            [clojure.tools.logging :as log]))

;; The world hash should be stored here, as well as functions to manipulate it.


(defn initiate-world
  "Loads all area files in the area directory and readies them for use in the mud."
  []
  (let [
	world (seed/seed 'world.seed/the-world-seed)]
    (reduce (fn [new-world obj]
              (assoc new-world (:vname obj) (ref obj))
              ) {} world)))

(defonce ^{:doc "The master world hash, all game objects are here.
hashed by there vname."} the-world (ref (initiate-world)))
(defonce ^{:doc "A set of all pcs logged in. (vnames)"
	   :private true} pcs (ref #{}))
(defonce ^{:doc "A hash of all PC's mobile to there user hashes."
	   :private true} pcs-to-users (ref {}))
(defn pcs-logged-in []
  @pcs)


(defn lookup 
  "Returns the ref of x, x can be a vname or a object map"
  [x]
  (cond
   (symbol? x)
   (@the-world x)
   (map? x)
   (@the-world (:vname x))))


(defn to-obj 
  "Returns the object hash denoted by x,
x can be its vname, ref to the hash or the hash it self."
  [x]
  (cond 
   (symbol? x) 
   (if-let [result (lookup x)]
     @result
     (throw (Exception. (format "No such object '%s' in world." x))))
   (and (map? x) (some #(= (:type x) %) [:room :item :mobile]))
   x
   (and (instance? clojure.lang.Ref x) (contains? #{:room :item :mobile} (:type @x)))
   @x
   true
   (do (log/error "Couldnt figure out what %s is [x] for to-obj" x (and (instance? clojure.lang.Ref x)) @x)
       (throw (Exception. (format "Don't know how to get an object from '%s'" x))))))

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
   (throw (log/error "Couldnt figure out what %s is [x] for to-obj" x (and (instance? clojure.lang.Ref x)) @x)
    (Exception. (format "Don't know how to get an object from '%s'" x)))))



(defn get-user
  "Gets the user logged in on mobile, or nil if no ones logged in on it."
  [vname]
  (assert (or (map? vname) (symbol? vname)))
  (pcs-to-users (if (map? vname)
		    (:vname vname)
		    vname)))

(defn object?
  "Returns true if the object is an object in the world database (not necessarly in a room or inventory)"
  [obj]
  (let [obj (to-obj obj)]
    (if (and obj (map? obj) (:vname obj) (the-world (:vname obj)))
      true false)))

(defn mobile? 
  "Returns true if obj is a mobile and in the world database (not necessarly in a room or active)"
  [obj]
  (let [obj (to-obj obj)]
    (if (and (object? obj) (= (:type obj) :mobile))
      true false)))


(defn pc?
  "Returns true if obj is a pc (and is loaded in the world and active)"
  [obj]
  (let [obj (to-obj obj)]
    (if (and (object? obj) (= (:type obj) :mobile) (@pcs (:vname obj)))
      true false)))

(defn npc?
  "Returns true if obj is a npc, is in the world and has a :soul fun. (but isn't a PC)"
  [obj]
  (let [obj (to-obj obj)]
    (if (and (object? obj) (= (:type obj) :mobile) (not (@pcs (:vname obj))) (fn? (:soul obj)))
      true false)))

(defn item?
  "Returns true if the obj is a item (and an object in the world)"
  [obj]
  (let [obj (to-obj obj)]
    (if (and (object? obj) (= (:type obj) :item))
      true false)))

(defn room? 
  "Returns true if the obj is a room (and an object in the world.)"
  [obj]
  (let [obj (to-obj obj)]
    (if (and (object? obj) (= (:type obj) :room))
      true false)))


(defn world-print 
  "testing function
pretty-prints an object in the world, or the hole world, with a print level of 8 (to keep from recursing to much)"
  ([]
     (with-bindings {#'*print-level* 8}
       (clojure.pprint/pprint @the-world)))
  ([vname]
     (with-bindings {#'*print-level* 8}
       (clojure.pprint/pprint (@the-world vname)))))



(defmacro safe-obj-print
  "Usefull for printing references without infinitely recursing on there references"
  [& args]

  `(with-bindings {#'*print-level* 8}
     ~@args))

;; (defn raw-contents-set 
;;   ([obj]
;;      (raw-contents-set obj #{}))
;;   ([obj acc]
;;      (let [obj (to-obj obj)]
;;        (apply clojure.set/union (:contents obj) 
;;               (for [cobj (:contents obj)]
;;                 (raw-contents-set (to-obj cobj) acc))))))



;(hooks/def-hook pc-enter-world)

(defn enter 
  "Enters a user (with a valid :character) into the world, called by user's socket managment function after login."
  [user items]
  (dosync
   (assert (:character user))
   (assert (:location @(:character user)))
   (assert (@the-world (:location @(:character user))))
   (let [{ch :character} user
         new-loc (@the-world (:location @ch))]
     (alter new-loc assoc  :contents (conj (:contents @new-loc) (:vname @ch)))
     (alter ch assoc :soul soul/pc-soul)
     (alter the-world assoc (:vname @ch) ch)
     (doseq [item items]
       (alter the-world assoc (:vname item) (ref item)))
     (alter pcs conj (:vname  @ch))
     (commute pcs-to-users assoc (:vname @ch) user))))

;(hooks/def-hook pc-leave-world)

(defn leave 
  "Removes the character cleany from the world, used by the user's connection managment function. 
for normal dismissing of users call (disconnect user) or (disconnect (:user @ch))"
  [user]
  (apply pc-file/save-pc 
   (dosync
    (let [{ch :character} user
          items (map to-obj-ref (:contents @ch))
          loc (@the-world (:location @ch))]
      
      (alter loc assoc :contents (disj (:contents @loc) (:vname @ch)))
     (alter ch dissoc :soul)
     (alter the-world dissoc  (:vname @ch))
     (doseq [item items]
       (alter the-world dissoc (:vname @item)))
     (alter pcs disj (:vname @ch))
     (commute pcs-to-users dissoc (:vname @ch))
     [@(:account user) @ch (map deref items)])))
  user)

