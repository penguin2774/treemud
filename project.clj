(defproject treemud "0.1.0-SNAPSHOT"
  :description "A mud server written in clojure with a diku command style."
  :url "https://github.com/penguin2774/treemud"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"] 
                 [org.clojure/tools.logging "0.2.6"] 
                 [log4j/log4j "1.2.17"]
                 ]
  :main  treemud.core
  :target-path "target/%s"
  :source-paths ["src"]
  :profiles {:uberjar {:aot :all}})
