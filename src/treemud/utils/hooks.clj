;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
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
