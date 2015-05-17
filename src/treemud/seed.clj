;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; o-O-o                 o   o         o  ;;
;;   |                   |\ /|         |  ;;
;;   |   o-o o-o o-o     | O | o  o  o-O  ;;
;;   |   |   |-' |-'     |   | |  | |  |  ;;
;;   o   o   o-o o-o     o   o o--o  o-o  ;;
;;                                        ;;
;; COPYRIGHT © 2015 Nathanael Cunningham  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Seed definition and instancing functions.
;; For programmatic object instancing.

(ns treemud.seed
  (:require [rielib.utils :as utils]
            [treemud.consts :as consts]
            [clojure.walk :as walkies]
            [contrib.except :as except])

  (:import [java.util UUID]))

(defn- create-vname
    "Creates a symbol with the namespace and name of sname#UUID where UUID is java's UUID string."
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


(defonce the-seeds (ref {}))

(defn add-seed 
  "Inserts a seed into the-seeds hash.
For most uses you'll want to def-seed instead."
  [sname seed]
  (assert (symbol? sname)
          (fn? seed))
  (dosync 
   (alter the-seeds assoc sname seed)
   sname))

(declare expand-seed-obj)

(defn seed 
  "Pruduces a new object(s) with the result of the named seed function, as well as any objects inside of it (with meta data sname true) or otherwise associated with it (depending on the seed's code). Returns all objects in a seq.
settings is a map that is passed to the seed function.
attribs is a map that overwrites valuse in the objects produced by seed function.
"
  [name & [settings attribs]]
  (letfn [(set-defaults [obj]
            (merge  (case (or  (:type obj) 
                               (except/throwf "%s seed produced object with no type." name))
                      :item item-defaults
                      :room room-defaults
                      :npc npc-defaults
                      :pc pc-defaults
                      :mobile mobile-defaults
                      (except/throwf "%s has unknown type %s" name (:type obj))) ; should use either npc or pc
                       
                    (assoc obj :vname (or (:vname obj)
                                          (create-vname name))) attribs {:sname  name}))

          (expand-seed-obj 
            ([obj]
             (let [acc-atom (atom [])
                   vname (:vname obj)
                   sname (:sname obj)]
               (assert (symbol? vname) "Object was not generated properly.")
               (letfn [(set-attribs-imbedded-sname [type] ; returns a map for the imbedded subobject's attribs
                                                          ; default is :contains (sets the subobject's location to obj's vname)
                         (case type
                           :contains
                           {:location vname}
                           :exit 
                           nil
                           true
                           {:location vname}
                           (except/throwf "Unknown imbedded sname type %s" type)))
                       (seed-if-sname [obj]
                         (cond (and (symbol? obj)
                                    (:sname (meta obj)))
                               (let [[new-obj & others :as all] (seed obj nil (set-attribs-imbedded-sname ((meta obj) :sname)))]
                                 (swap! acc-atom concat all)
                                 (:vname new-obj))
                               (and (vector? obj)
                                    (symbol? (first obj))
                                    (:sname (meta (first obj))))
                               (let [[sname settings attribs]  obj
                                     [new-obj & others :as all] (seed sname settings (merge attribs (set-attribs-imbedded-sname ((meta sname) :sname)))) ]
                                 (swap! acc-atom concat all)
                                 (:vname new-obj))
                               :else
                               obj))]
                 (cons  (assoc (walkies/prewalk seed-if-sname (dissoc obj :sname) )  :sname sname) @acc-atom)))))]

    (if-let [seed (@the-seeds name)]
      (let [obj (apply seed [settings])]
        (expand-seed-obj (set-defaults obj)))
      (except/throwf "No such seed %s" name))))



(defmacro def-seed 
  "Defines a new seed and adds it to the-seeds.
Seeds need to be functions that return a single map of the object.
The object can contain symbols that have there :sname metadata filled in with either
true, :contains or :exit (currently). These will cause the named seed to be added to the result of a seed call. ^{:sname :contains} or simply ^:sname will set the subobject's :location to the vname for the generating object. ^{:sname :exit} will not set anything on the subobject (for room exits that generate more rooms.).
a doc string can be provided as with (defn).
A variable will be defiled with the same name as the sname. It will be set to the sname.
So  (def-seed sword ...) (seed sword) will work."

  [sname & fn-desc ]

  (let [sname-wdoc (with-meta (symbol (str *ns*) (str sname))
                     {:doc 
                      (if (string? (first fn-desc))
                        (first fn-desc)
                        nil)})]
    `(def ~sname-wdoc
       (add-seed '~sname-wdoc (fn ~sname ~@(if (string? (first fn-desc))
                                 (next fn-desc)
                                 fn-desc))))))


