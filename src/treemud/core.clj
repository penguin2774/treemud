(ns treemud.core
  (:gen-class)
  (:require rielib.utils
            treemud.server))


(defn launch-server []
  (rielib.utils/launch-thread treemud.server/main))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
