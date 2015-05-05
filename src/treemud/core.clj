(ns treemud.core
  (:gen-class)
  (:require rielib.utils
            treemud.server
            treemud.npc
            treemud.tick
            [clojure.tools.logging :as log]))



(defn launch-server []
  (log/info "Launching server thread.")
  (rielib.utils/launch-thread treemud.server/main)
  (log/info "Launching npc thread.")
  (treemud.npc/launch-npc-process-actions-thread)
  (log/info "Launching tick thread.")
  (treemud.tick/launch-master-tick-thread)
  (log/info "Startup Complete"))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
