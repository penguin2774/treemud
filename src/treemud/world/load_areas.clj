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



;; Functions for loading the area files from disk.
(ns treemud.world.load-areas
  (:use [contrib except] 
        [clojure.java.io :as io]
        ))






		       
(defn load-areas
  "Loads all the area files in areas/ folder, loading them into a 
hash by there vnames (uninitialized)"
  []
  (let [objects (mapcat (fn [f]
			  (with-open [in (java.io.PushbackReader. (io/reader f))] 
			   (loop [acc [] obj (read in nil :eof)]
			     (if-not (= obj :eof)
			       (recur (conj acc obj) (read in nil :eof))
			       acc))))
			  (filter #(and 
				    (.isFile %)
				    (.endsWith (.getName %) ".area"))
				  (file-seq (java.io.File. "areas/"))))
	world-map (reduce (fn [acc obj]
			    (throw-if-not (:vname obj) Exception "Missing vname for object %s" obj)
			    (throw-if (acc (:vname obj)) RuntimeException "There are two objecs with the vname %s"
				      (:vname obj))
			   (assoc acc (:vname obj) obj)) {}
			 objects)]
    world-map))
     
