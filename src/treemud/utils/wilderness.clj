;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2010 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Functions for working with the wilderness area, mostly test funcitons right now. 

(ns treemud.utils.wilderness)


(defn make-test-map
  "Makes a gient hash of thick woods, for memory usage testing."
  []
  (letfn [(make-tile [x y]
		     {:desc "You're in a thick woodland."
		      :name "A Thick woodland."
		      :vname (symbol (format "wilderness.%d-%d"
					     x y))
		      :exits (reduce (fn [exits [dir [x2 y2]]]
				       (assoc exits  dir (symbol (format "wilderness.%d-%d" x2 y2)))) {}
				       (reduce (fn [acc [pos [x2 y2]]]
						 (let [x3 (+ x x2)
						       y3 (+ y y2)]
						   (if (and (<= 0 x3 640)
							    (<= 0 y3 480))
						     (assoc acc pos [x3 y3])
						     acc))) {} {:nw [-1 -1] :n [0 -1] :ne [1 -1]
						 :w [-1 0] :e [1 0]
						 :sw [-1 1] :s [0 1] :se [1 1]}) )})]
    (let [wm (reduce (fn [acc [x y]]
		       (let [tile (make-tile x y)]
			 (assoc acc (:vname tile) tile))) {}
			 (for [x (range 641)
			       y (range 481)]
			   [x y]))]
      nil)))