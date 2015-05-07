

(ns treemud.seed
  (:require [rielib.utils :as utils]
            [treemud.consts :as consts]
            [clojure.walk :as walkies])

  (:import [java.util UUID] ))

(defn- create-vname
    "Gets the next unused number, this could be a problem when dealing
with PC's saved inventory, which are not considered"
    ([sname]
       (symbol (str (name sname) "#" (UUID/randomUUID)))))


(def room-defaults {:name "A generic room"
		       :desc "Clearly this room requires some love, as its missing a description..."
		       :contents #{}
		       :exits {}
		       :resets []})

(def item-defaults  {:name "lump of clay"
		     :short "a small lump of clay"
		     :long "A small lump of clay lays abandon here."
		     :location consts/default-room})


(def mobile-defaults {:name "Larry"
		      :short "an unremarkable human peasant"
		      :long "An unassuming human peasent is here, looking around bewildered."
		      :race :human
		      :sex :male
		      :levels {:commoner 1}
		      :hps 4
		      :base-attack 0
		      :stats {:str 10
			      :dex 10
			      :con 10
			      :int 10
			      :wis 10
			      :cha 10}

		      :contents #{}
		      :ac 10
		      :skills {:spot 4
			       :profession-farming 4}
		      :feats []
		      :location consts/default-room})

(def npc-defaults (merge mobile-defaults
                        {}))

(def pc-defaults (merge mobile-defaults 
                        {}))


(def the-seeds (ref {}))

(defn add-seed [sname seed]
  (assert (symbol? sname)
          (fn? seed))
  (dosync 
   (alter the-seeds assoc sname seed)))

(defn seed [name & [arg]]
  (if-let [seed (@the-seeds name)]
    (apply seed [(or arg {})])))

(defmacro def-seed [sname & fn-desc ]
  `(add-seed '~sname (fn ~sname ~@fn-desc)))

(def-seed equip.sword.short-sword [{status :status location :location}]
  (let [vname (create-vname 'equip.sword.short-sword)
        status (or status (utils/choice :good :alright :terrible))
        status-strs {:good "well made"
                     :alright ""
                     :terrible "awful"}]
    [{:vname vname
       :sname 'equip.sword.short-sword
       :type :item
       :name "a short sword"
       :location location
       :short (format "a %s short sword" (status-strs status))
       :long (format "a %s short sword is left here." (status-strs status))}]))

(def-seed useless.rock [{location :location}]
  (let [vname (create-vname 'useless.rock)]
    [{:vname vname
      :sname 'useless.rock
      :type :item
      :location location
      :name "rock"
      :short "a small granite rock"
      :long "a small granite rock is here."}]))

(defn expand-seed-obj 
  ([obj]
   (let [acc-atom (atom [])
         vname (:vname obj)]
     (assert (symbol? vname) "Object was not generated properly.")
     (letfn [(seed-if-sname [obj]
               (cond (and (symbol? obj)
                          (:sname (meta obj)))
                     (let [[new-obj & others :as all] (seed obj {:location vname})]
                       (swap! acc-atom concat all)
                       (:vname new-obj))
                     (and (vector? obj)
                          (symbol? (first obj))
                          (:sname (meta (first obj))))
                     (let [[sname data] obj
                           [new-obj & others :as all] (seed sname (merge data {:location vname})) ]
                       (swap! acc-atom concat all)
                       (:vname new-obj))
                     :else
                     obj))]
       (cons  (walkies/prewalk seed-if-sname obj ) @acc-atom)))))

(def-seed equip.backpack [{location :location}]
  (let [vname (create-vname 'equip.backpack)]
    (expand-seed-obj {:vname vname
                      :sname 'equip.backpack
                      :type :item
                      :location location
                      :contents #{['^:sname useless.rock {} 1]
                                  ['^:sname useless.rock {} 2]
                                  ['^:sname useless.rock {} 3]
                                  ['^:sname useless.rock {} 4]}
                      :name "backpack"
                      :short "a leather backpack"
                      :long "a leather backpack is here."})))

;; (defn gen-objects [seed-name seed-obj]
;;   (let [vname (create-vname seed-name)
;;         [new-seed-obj other-new-objs] (expand-seed-obj seed-obj)]
;;     {vname (merge {:vname vname
;;                    :sname seed-name 
;;                    } new-seed-obj )})
;;   )




(def-seed humanoid.orc []
  (let [vname (create-vname 'humanoid.orc)]
    {vname {:vname vname
            :sname 'humanoid.orc
            :type :mobile
            :name "an orc"
            :short "A tall male orc"
            :long "A tall male orc is here."
            :equipment {:wield 'equip.sword.short-sword}}}))
