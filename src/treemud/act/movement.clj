;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; :author Nathanael Cunningham
;; :doc All action functions pertaining to movement.

(ns treemud.act.movement
  (:use contrib.except)
  (:require [treemud.world :as world]
	    [treemud.world.change :as change]
	    [treemud.event :as event]))



(defn move 
  "Moves the mobile in the dir direction, dir should be a string naming the exit, and ch can be any mobile. The exit is grabed from the ch's location. 
Calls: 
On Success:
  (act old-loc :left-room    ch dir)
  (act ch      :left-room    ch dir)
  (act new     :entered-room ch dir)
On Failure
No such exit
  (act ch      :cant-move ch :no-such-exit)"
  [ch  #^String dir]
  (throw-if-not (world/mobile? ch) IllegalArgumentException "Only mobiles can use act/move")
  (throw-if-not (string? dir) IllegalArgumentException "dir must be a string.")
  (letfn [(perform-move! []
	    (dosync 
		(let [ch (world/to-obj-ref ch)
		      old-loc (world/to-obj-ref (:location @ch))]
		  (if ((:exits @old-loc) dir)
		    (let [new-loc (world/to-obj-ref ((:exits @old-loc) dir))]
		      (change/location ch new-loc)
		      [@ch @old-loc @new-loc])
		    :no-exit))))]
    
    (let [result (perform-move!)]
      (if (vector? result)
	(let [[ch old new] result]
	  (event/act old :left-room ch dir)
	  (event/act ch :left-room ch dir)
	  (event/act new :entered-room ch dir))
	(event/act ch :cant-move ch :no-such-exit)))))
      
;; (defn teleport 
;;   [ch loc]
;;   (throw-if-not (world/mobile? ch) IllegalArgumentException "Only mobiles can use act/move") 
;;   (letfn [(perform-move! 
;;             (dosync 
;;              (let [ch (world/to-obj-ref ch)
;;                    old-loc (world/to-obj-ref (:location @ch))
;;                    new-loc (world/to-obj-ref loc)]
;;                (change/location ch new-loc)
;;                [@ch @old-loc @new-loc]
;;                :no-exit)))]))
