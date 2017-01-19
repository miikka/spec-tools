(ns spec-tools.json-schema.simplify
  "Simplification of JSON Schemas."
  (:require [clojure.core.logic :as logic]))

(logic/defne only-one? [a coll]
  ([_ _] (logic/membero a coll)))

(logic/defne singleton
  "A relation where l is a collection, such that l has exactly one element"
  [l]
  ([[_ . ()]]))

(logic/defne singleton-map
  [x l]
  ([_ {x _}]))

(defmacro trace [x]
  `(let [x# ~x]
     (prn :TRACE '~x x#)
     x#))

(defn simplify-all-of
  [schema]
  (or (first (logic/run 1 [q]
               (logic/fresh [all-of]
                 (logic/featurec schema {:allOf all-of})
                 (conde
                   [(logic/emptyo all-of) (logic/== q (dissoc schema :allOf))]
                   [(singleton all-of)
                    (singleton-map :allOf schema)
                    (logic/membero q all-of)]))))
      schema))
