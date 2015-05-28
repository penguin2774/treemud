;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; COPYRIGHT © 2010 Nathanael Cunningham, all rights reserved
;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the file LICENSE at the root of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;       the terms of this license.
;;  You must not remove this notice, or any other, from this software.




(ns treemud.core
  (:gen-class)
  (:require treemud.utils
            treemud.server
            treemud.npc
            treemud.tick
            [clojure.tools.logging :as log]))



(defn launch-server []
  (log/info "Launching server thread.")
  (treemud.utils/launch-thread treemud.server/main)
  (log/info "Launching npc thread.")
  (treemud.npc/launch-npc-process-actions-thread)
  (log/info "Launching tick thread.")
  (treemud.tick/launch-master-tick-thread)
  (log/info "Startup Complete"))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
