;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2015 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; COPYRIGHT © 2015 Nathanael Cunningham, all rights reserved
;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the file LICENSE at the root of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;       the terms of this license.
;;  You must not remove this notice, or any other, from this software.


;; Miscellanies utilities

(ns treemud.utils
  (:import [java.io.StringBuffer]
           (java.security MessageDigest)))

(def ^:private out-stash *out*)


(defn- format-error [err]
  (let [e (last (take-while (complement nil?) (iterate #(.getCause %) err)))
	buff (new StringBuffer)]
    (.append buff (println-str (if (instance? Exception e)
				"I take EXCEPTION to that: "
				"ERROR!") (class err) (if (nil? (.getMessage err))
					    (.getMessage e)
					    (.getMessage err))))
    (loop [this (first (.getStackTrace e)) n (next (.getStackTrace e))]
      (.append buff (println-str (format "%24s[%4d]:%s.%s" 
					 (if (.getFileName this)
					   (.getFileName this)
					   "Repl?")
					 (.getLineNumber this) 
					 (.getClassName this) 
					 (.getMethodName this))))
      (if n
	(recur (first n) (next n))
	nil))
    (str buff)))


(defn- print-error [err]
  (println (format-error err)))

(defmacro thread-safe [& body]
  (let [e-gen (gensym)]
    `(binding [*out* out-stash]
       (let [result# 
	     (try ;;  ~(if debug
;; 			'(println "Thread" (.getName (Thread/currentThread))  "is go!"))
		  ~@body
		  (catch Throwable ~e-gen
		    (println (.getName (Thread/currentThread)) " pulled some kind of shit!")
		    (print-error ~e-gen))
		  ;; ~(if debug '(finally 
;; 			       (println "Thread" (.getName (Thread/currentThread)) "Over!")))
		  )]
	 result#))))



(defn launch-thread 
  "Launches func on a new thread. Catching output and exceptions and printing them 
to the *out* as its bound before the thread is launched. Making sure you get the output. "
  [func & args]
  (let [thread (new Thread (fn [] (thread-safe (apply func args))))]
    (.start thread)
    thread))


(defn hexstr 
  "Converts an array of Numbers into a string of hexadecimal values."
  [bytes]
  (apply str (map #(format "%x" %) (seq bytes))))


(defn digest-message 
  "Usage java.security.MessageDigest to digest a string. If the algotrithm flag is supplyed, it is used
and if str? is true, then the result is passed through (hexstr)"
  ([message]
     (digest-message message {:algorithm "SHA1"}))
  ([message {:keys [algorithm str?]}]
    (let [result (.digest (MessageDigest/getInstance (or algorithm "SHA1")) 
			  (condp instance? message
			    String
			    (.getBytes message)
			    message))]
      (if str?
	(hexstr result)
	result))))
	    



(defn roll-die
  "Simulates a dice roll with an x sided dice."
  [sides]
  (+ 1 (rand-int sides)))

(defn dnd-dice
  "Simulates a dice roll with a D20 dice specification."
  [num sides mod]
  (reduce + (conj  (for [_ num]
                     (roll-die sides)) mod)))

(defn die-seq [sides]
  (lazy-seq (cons (roll-die sides) (die-seq sides))))

(defn choice 
  ([f & r]
     (choice (cons f r)))
  ([s]
     (let [s (seq s)]
       (nth s (rand (count s))))))

