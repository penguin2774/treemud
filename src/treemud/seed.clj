

(ns treemud.seed
  (:require [rielib.utils :as utils]
            [treemud.consts :as consts]
            [clojure.walk :as walkies]
            [contrib.except :as except])

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

(declare expand-seed-obj)

(defn seed [name & [attribs settings]]
  (if-let [seed (@the-seeds name)]
    (let [[obj & subobjs] (apply seed [settings])
          obj  (merge  (case (or  (:type obj) 
                                  (except/throwf "%s seed produced object with no type." name))
                         :item item-defaults
                         :room room-defaults
                         :npc npc-defaults
                         :pc pc-defaults
                         :mobile mobile-defaults
                         (except/throwf "%s has unknown type %s" name (:type obj))) ; should use either npc or pc
                       
                       (assoc obj :vname (or (:vname obj)
                                             (create-vname name))) attribs {:sname  name})]
         (concat (expand-seed-obj obj) subobjs))))

(defn expand-seed-obj 
  ([obj]
   (let [acc-atom (atom [])
         vname (:vname obj)
         sname (:sname obj)]
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
                     (let [[sname attribs settings] obj
                           [new-obj & others :as all] (seed sname (merge attribs {:location vname}) settings) ]
                       (swap! acc-atom concat all)
                       (:vname new-obj))
                     :else
                     obj))]
       (cons  (assoc (walkies/prewalk seed-if-sname (dissoc obj :sname) )  :sname sname) @acc-atom)))))

(defmacro def-seed [sname & fn-desc ]
  `(add-seed '~(symbol (str *ns*) (str sname)) (fn ~sname ~@fn-desc)))

(def-seed short-sword [{status :status }]
  (let [status (or status (utils/choice :good :alright :terrible))
        status-strs {:good "well made"
                     :alright ""
                     :terrible "awful"}]
    [{ :sname 'equip.sword.short-sword
       :type :item
       :name "a short sword"
       :short (format "a %s short sword" (status-strs status))
       :long (format "a %s short sword is left here." (status-strs status))}]))

(def-seed rock [_]
  (let [vname (create-vname 'treemud.seed/rock)]
    [{:vname vname
      :sname 'useless.rock
      :type :item
      :name "rock"
      :short "a small granite rock"
      :long "a small granite rock is here."}]))



(def-seed backpack [_]
  [{ :type :item
     :contents #{['^:sname treemud.seed/rock {} nil 1]
                 ['^:sname treemud.seed/rock {} nil 2]
                 ['^:sname treemud.seed/rock {} nil 3]
                 ['^:sname treemud.seed/rock {} nil 4]}
     :name "backpack"
     :short "a leather backpack"
     :long "a leather backpack is here."}])


(def-seed orc [_]
  [{:type :npc
    :name "an orc"
    :short "A tall male orc"
    :long "A tall male orc is here."
    :equipment {:wield '^:sname treemud.seed/short-sword
               :back '^:sname treemud.seed/backpack}
    }])
