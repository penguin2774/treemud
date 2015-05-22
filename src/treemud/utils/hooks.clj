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



;; Macros for dealing with hooks




(ns treemud.utils.hooks)

(defmacro def-hook [name]
  (let [hook-var (symbol (format "*%s-hooks*" name))]
    `(do (def ~hook-var (atom #{}))
         (defn ~(symbol (format "hook-%s" name)) [fn#]
           (swap! ~hook-var conj fn#))
         (defn ~(symbol (format "call-%s-hooks" name)) [& args#]
           (let [fns# (deref ~hook-var)]
             (doseq [fn# fns#]
               (apply fn# args#)))))))
