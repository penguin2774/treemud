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
;; :doc All the command parser functions, command lookup functions, the command table itself, as well as def-command.

(ns 
  treemud.command.parse)

(defonce *commands* (atom {}))


(declare compair-argsets)

(defn def-command 
  "Defines a command in the command parsers hash. 
fn being the function that will take the command, and argset defines the arguments the function will expect.
argset:
\"command-string\" args...
args can be:
:word - a single word or 'quoted string'
:object - synonym for word
\"string\" - a spesific string, used to differ it from simmilar commands.
 [\"string\" \"string\"] - a vector of strings, arg must match one of those strings.
:rest - a string containing all remaining arguments."
  [fn & argset]
  (assert (string? (first argset)))
  (let [letter (nth (first argset) 0)
	command-set (@*commands* letter)]
    (if command-set
      (reset! *commands* (assoc @*commands* letter (assoc command-set argset fn)))
      (reset! *commands* (assoc @*commands* letter 
			      (sorted-map-by compair-argsets argset fn))))))
      

     
(declare parse-command)

(defn lookup-command 
  "Takes an input string and looks for a command that matches it.
returns its function if found, otherwise nil."
  [input]
  (if-let [cmds (and (not (empty? input))
		     (@*commands* (nth input 0)))]
    (let [cmd (re-find #"\w+" input)
	  args (.trim (.substring input (count cmd)))]
    (some (fn [[cmd-argset cmd-fn]]
	    (if-let [result (parse-command cmd args cmd-argset)]
		     [cmd-fn result]))
	    cmds))))


(defn- compair-argsets 
  "Compair two argsets, for sorting. Makes sure the one with the more descript spesification comes up first."
  [set1 set2]
  (if (= (first set1) (first set2))
    (if (> (count set2) (count set1))
      1
      (if (= (count set2) (count set1))
	(- (.hashCode set2) (.hashCode set1))
	-1))
    (.compareTo (first set1) (first set2))))
  
					; format ["command" ["option1" "option2"] :object :rest]

(defn parse-command
  "takes a command command string and a args string checks it to the argset, if successful it returns the command and args as specified. Otherwise false."
  [cmd args command-argset]
  (if (.startsWith (first command-argset) cmd)
    (loop [argset (next command-argset) args args result [(first command-argset)]]
      (cond
       (nil? argset)
       result
       
       (empty? args)
       false
       
       (string? (first argset))
       (if (.startsWith args (first argset))
	 (recur (next argset) (.trim (.substring args (count (first argset)))) (conj result (first argset)))
	 false)
       
       (vector? (first argset))
       (if-let [arg (some #(if (.startsWith args %) %) (first argset))]
	 (recur (next argset) (.trim (.substring args (count arg))) (conj result arg))
	 false)
       
       (keyword? (first argset))
       (condp = (first argset)
	 :rest
	 (conj result args)
	 :word
	 (if-let [obj (re-find #"\w+|'.+'" args)]
	   (recur (next argset) (.trim (.substring args (count obj))) (conj result obj))  ; TODO: needs to get the actual object
	   false)
	 :object
	 (if-let [obj (re-find #"\w+|'.+'" args)]
	   (recur (next argset) (.trim (.substring args (count obj))) (conj result obj))  ; TODO: needs to get the actual object
	   false)
	 (throw (Exception. (format "Command has invalid command-arugment keyword %s" (first argset)))))))))