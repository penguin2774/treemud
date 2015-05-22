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



;; Contains debugging behaviors 

(ns treemud.npc.debug
  (:require [treemud.npc :as npc]
            [treemud.world :as world]
            [treemud.server :as server]
            [treemud.event.soul :as pc-soul]
            [treemud.server.comm :as comm]
            [clojure.tools.logging :as log]
            [treemud.event :as event]
            [treemud.utils.color :as color]
            [clojure.string :as string])

  (:import [java.io StringWriter]))



(npc/define-behavior spy [e self cause & data]

  (if-let [master (self :master)] ; There is such a user in the world
    (if-let [master-user (world/get-user master)] ; and there logged in.
      (let [string-catcher (StringWriter.)
            fake-user  (merge master-user {:out string-catcher})
            fake-self (assoc self :soul (merge master-user {:out string-catcher}))]
        (with-bindings {#'event/*user* fake-user
                        #'color/*colors?* (:ansi-color? fake-user)}
          (apply pc-soul/pc-soul e self cause data)
          (let [result (.toString string-catcher)]
            (if-not (.isEmpty result)
              (comm/sendln master-user "%s saw: %s" (:name self) (string/replace result ":treemud>" "") )))))))
  [])
