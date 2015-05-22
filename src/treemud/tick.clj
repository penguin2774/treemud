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

;; Holds the master 'tick' function and all timming mechanics


(ns treemud.tick
  (:require [rielib.utils :as utils]
            [clojure.tools.logging :as log]))

(def ^{:private true} master-tick-running (atom false))

(defonce tick-fns (ref {}))

(defn add-tick [name ticks fn]
  (assert (symbol? name))
  (assert (number? ticks))
  (assert (fn? fn))
  (dosync (alter tick-fns assoc name {:fn fn :ticks ticks})))


(defmacro def-tick 
  "Defines a tick function to be called by the master-tick thread.
ticks is the number of ticks inbetween calls."
  [name ticks & body]
  (let [name-ns (symbol (str *ns*) (str name))]
    `(add-tick '~name-ns ~ticks
               (fn ~name [] ~@body))))

(def master-tick-milliseconds 1000)


(defonce master-tick-running (atom false))

(defn- master-tick [tick-count]
  (doseq [{fun :fn ticks :ticks} (vals @tick-fns)]
    (if (= (rem tick-count ticks) 0)
      (try 
        (fun)
        (catch Exception e
          (log/error e "Tick fn %s failed with error: %s" fun (.getMessage e)))
        (catch AssertionError e
          (log/error e "Tick fn %s failed assertion with error: %s" fun (.getMessage e)))))))




(defn launch-master-tick-thread 
  "Launches the master tick thread if not already running, returning the thread object. 
Otherwise returns nil."
  []
  (if (not @master-tick-running)
    (utils/launch-thread 
     (fn []
       (log/info "master-tick-thread Launching...")
       (reset! master-tick-running true) ; there is no master-tick thread running
       (loop [last-tick (System/currentTimeMillis) total-ticks 0]
         (master-tick total-ticks)
         (let [elapsed-time (- (System/currentTimeMillis) last-tick)
               sleep-time (- master-tick-milliseconds elapsed-time) ]
           (if (> sleep-time 0)
             (Thread/sleep sleep-time)
             (log/warn "More time elapsed running master-tick then sleep time. CPU being worked hard :/ elapsed-time [" elapsed-time "ms]")))
         (if @master-tick-running
           (recur (System/currentTimeMillis) (inc total-ticks))))
       (log/info "master-tick-thread Exiting...")
       (reset! master-tick-running false)))))

(defn terminate-master-tick-thread []
  (reset! master-tick-running false))
