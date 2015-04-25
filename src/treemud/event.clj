;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Contains the event system, as well as the act function, which is essential for act/x functions.
(ns treemud.event
  (:use contrib.except)
  (:require 
	    [treemud.world :as world]
	    [treemud.world.object :as object]
	    [treemud.event.soul :as soul]
	    [treemud.server.comm :as comm]
	    [treemud.utils.color :as color]))


;; Event launching functions, and event messanger def macros go here.



(def ^:dynamic *user* nil)

(defn act
  "Issues an event to obj. If obj is a room, it is sent to all mobiles in the room.
 (Note: it actualy gets the obj's :soul function and calls that. Which should, if its a PC, be event.soul/pc-soul funciton.)
Arguments:
obj   is the target
e     is the event,
cause is the object that caused it
data  can be any relevent data, depending on the event, to find what an event needs, check out its PC event handler (usualy by a related command)."
  [obj e cause & data]
  (let [obj (world/to-obj obj)
	cause (world/to-obj cause)]
    (condp = (:type obj)
      :mobile
      (if (obj :soul)
	(if-let [user (world/get-user obj)]
	  (with-bindings {#'*user* user
			  #'color/*colors?* (:ansi-color? user)}
	    (do (assert (fn? (obj :soul)))
		(apply (obj :soul) e obj cause data)))
	    (do (assert (fn? (obj :soul)))
		(apply (obj :soul) e obj cause data))))
      :room
      (doseq [m (object/contents obj :mobile)]
	(apply act m e cause data))
      :item
      (if (obj :soul)
	(if-let [user (world/get-user obj)]
	  (with-bindings {#'*user* user
			  #'color/*colors?* (:ansi-color? user)}
	    (do (assert (fn? (obj :soul)))
		(apply (obj :soul) e obj cause data)))
	    (do (assert (fn? (obj :soul)))
		(apply (obj :soul) e obj cause data)))))))


(defn tell
  "Sends a message to the PC in context, only works in def-event-handler(s)."
  ([message]
     (throw-if-not *user* IllegalArgumentException "event/tell must be called from within a event handler.")
     (comm/send-message *user* message))
  ([message & args]
     (throw-if-not *user* IllegalArgumentException "event/tell must be called from within a event handler.")
     (apply comm/send-message *user* message args)))

(defn tellln 
  "Sends a message to the PC in context with a trailing end line, only works in def-event-handler(s)."
  ([message]
     (throw-if-not *user* IllegalArgumentException  "event/tellln must be called from within a event handler.")
     (comm/sendln *user* message))
  ([message & args]
     (throw-if-not *user* IllegalArgumentException  "event/tellln must be called from within a event handler.")
     (apply comm/sendln *user* message args)))

(defmacro with-reprompting 
  "Resends the prompt to the PC in context once body has finnished. This is done automaticly in def-event-handler when ch is not the cause (not the command caller). Should not be used unless your sure you know what you're doing."
  [& body]
  `(do (throw-if-not *user* IllegalArgumentException  "event/with-reprompting must be called from within a event handler.")
       ~(if (and (nil? (first body)) (empty? (rest body))) ; body is nil
	  nil
	  `(comm/with-reprompting *user*
	     ~@body))))


(defmacro def-event-handler
  "Defines an event handler for PCs, This is used to turn events into usefull text output.
event: the event's keyword
args : the arguments passed to the event, first is allways the ch being informed,
second is always the cause of the event, and the rest is event dependent.
this : when the cause is the same as the pc being informed, this part is executed
other: when the cause isn't the same as the pc being informed, this part is executed."
  [event args 
   this
   other]
      `(let [fn# (fn ~args
		   (if (= ~(first args) ~(second args))
		     ~this
		     (with-reprompting
		       ~other)))]
	 (soul/register-event ~event fn#)))

(defmacro color-str 
  "Colors the string based on cd, only use-able in a def-event-handler."
  [cd & str]
  `(color/color-str *user* ~cd ~@str))



(defn event-string-replace 
  "Replaces keywords in the col in the form of :target.object-data-function.
  object data functions, such as object/name or object/him-her must be in the form:
  subject viewer. Invalid function names will throw a 'ClassNotFoundException'"
  [msg-col viewer cause & victims]
  (clojure.string/join (map (fn  [x]
                               (cond 
                                 (string? x) x
                                 (keyword? x) (let [[target f] (clojure.string/split (str x) #"\.")]
                                                ((eval (symbol "treemud.world.object" f)) 
                                                 (cond 
                                                   (= target  ":self") cause
                                                   (= target  ":victim") (first victims)
                                                   (= target  ":other") (second victims)
                                                   (re-matches #":victim([0-9]+)" target ) (nth victims (Integer. (second  (re-matches #"victim([0-9]+)" target )))))
                                                 viewer))
                                 (or (list? x) (vector? x)) 
                                 ((fn eval-event-fun [[f & args]]
                                    (let [args-evaled (map (fn [target]
                                                             (cond 
                                                               (= target :viewer) viewer
                                                               (= target  :self) cause
                                                               (= target  :victim) (first victims)
                                                               (= target  :other) (second victims)
                                                               (re-matches #":victim([0-9]+)" (str target)) 
                                                               (nth victims 
                                                                    (Integer. 
                                                                     (second  (re-matches #"victim([0-9]+)" (str target) ))))
                                                               (and  (or  (list? target) (vector? target)) 
                                                                     (fn? (first target))) 
                                                               (eval-event-fun target)
                                                               true target)) args)]
                                      (apply f args-evaled))) x)
                                 (map? x) nil ; maps are used for adding data for mobiles to understand string easier.
                                 true (str x)))
                             msg-col)))







;; A work in progress...
;; (defn mud-str [ch objs & strs]
;;   (let [objs (vec objs)
;; 	he-she-it {:male "he"
;; 		   :female "she"
;; 		   :neuter "it"
;; 		   nil "it"}
;; 	him-her-it {:male "him"
;; 		    :female "her"
;; 		    :neuter "it"
;; 		    nil "it"}
;; 	his-her-its {:male "his"
;; 		     :female "her"
;; 		     :neuter "its"
;; 		     nil "its"}]
	
	
;;   (apply str 
;; 	 (loop [this (first strs) rest (next strs) result [] color? false]
;; 	   (if this
;; 	     (cond
;; 	      (and (keyword? this) (= :ln this))
;; 	      (recur (first rest) (next rest) (conj result "\n\r") color?)
;; 	      (keyword? this)
;; 	      (let [[color-codes rest] (split-with keyword? rest)]
;; 		(recur (first rest) (next rest) (conj result (apply color/make-color-code this color-codes)) true))
;; 	      (and (symbol? this) (re-matches #"\$\d*\w" (name this)))
;; 	      (let [[_ num code] (re-matches #"\$(\d*)(\w)" (name this))
;; 		    obj (if (empty? num) ch (nth objs (Integer. num)))]
;; 		(let [cr (condp = (first code)
;; 			   \n
;; 			   (object/name obj ch)
;; 			   \e
;; 			   (he-she-it (object/sex obj ch))
;; 			   \m 
;; 			   (him-her-it (object/sex obj ch))
;; 			   \s
;; 			   (his-her-its (object/sex obj ch))
;; 			   \p
;; 			   (object/short obj ch)
;; 			   (throwf IllegalArgumentException "There is no mud-str act code for %c" (first code)))]
;; 		  (recur (first rest) (next rest) (conj result cr) color?)))
;; 	      (string? this)
;; 	      (recur (first rest) (next rest) (conj result this) color?)
;; 	      true
;; 	      (recur (first rest) (next rest) (conj result (str this)) color?))
;; 	     (if color?
;; 	       (conj result (color/make-color-code :reset))
;; 	       result))))))

;; (defn mud-tell [ch objs & strs]
;;   (tellln (apply mud-str ch objs strs)))
