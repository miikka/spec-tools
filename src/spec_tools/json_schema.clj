(ns spec-tools.json-schema
  "Tools for converting specs into JSON Schemata."
  (:require [clojure.spec :as s]))

(defn- spec-dispatch
  [spec]
  (cond
    (or (s/spec? spec) (s/regex? spec) (keyword? spec))
    (let [form (s/form spec)]
      (if (not= form :clojure.spec/unknown)
        (if (seq? form)
          (first form)
          (spec-dispatch form))
        spec))
    (set? spec) ::set
    :else spec))

(defmulti to-json "Convert a spec into a JSON Schema." spec-dispatch :default ::default)

(defmethod to-json 'int? [spec] {:type "integer"})
(defmethod to-json 'clojure.core/int? [spec] {:type "integer"})
(defmethod to-json int? [spec] {:type "integer"})
(defmethod to-json integer? [spec] {:type "integer"})

(defmethod to-json float? [spec] {:type "number"})
(defmethod to-json double? [spec] {:type "number" :format "double"})

(defmethod to-json string? [spec] {:type "string"})
(defmethod to-json 'clojure.core/string? [spec] {:type "string"})

(defmethod to-json boolean? [spec] {:type "boolean"})

(defmethod to-json nil? [spec] {:type "null"})

(defmethod to-json inst? [spec] {:type "string" :format "date-time"})

(defmethod to-json 'clojure.core/pos? [spec] {:minimum 0 :exclusiveMinimum true})

(defmethod to-json ::set [spec]
  {:enum (vec (if (keyword? spec) (s/form spec) spec))})

(defmethod to-json 'clojure.spec/every [spec]
  (let [[_ inner-spec] (s/form spec)]
    {:type "array" :items (to-json inner-spec)}))

(defmethod to-json 'clojure.spec/tuple [spec]
  (let [[_ & inner-specs] (s/form spec)]
    {:type "array" :items (mapv to-json inner-specs) :minItems (count inner-specs)}))

(defmethod to-json 'clojure.spec/* [spec]
  (let [[_ inner-spec] (s/form spec)]
    {:type "array" :items (to-json inner-spec)}))

(defmethod to-json 'clojure.spec/+ [spec]
  (let [[_ inner-spec] (s/form spec)]
    {:type "array" :items (to-json inner-spec) :minItems 1}))

(defmethod to-json 'clojure.spec/keys [spec]
  (let [[_ & {:keys [req req-un opt opt-un]}] (s/form spec)
        properties (into {} (map (juxt name to-json)
                                 (concat req req-un opt opt-un)))]
    {:type "object"
     :properties properties
     :required (map name (concat req req-un))
     :additionalProperties false}))

(defmethod to-json 'clojure.spec/or [spec]
  (let [[_ & {:as inner-spec-map}] (s/form spec)]
    {:anyOf (mapv to-json (vals inner-spec-map))}))

(defmethod to-json 'clojure.spec/and [spec]
  (let [[_ & inner-specs] (s/form spec)]
    {:allOf (mapv to-json inner-specs)}))

(defmethod to-json ::default [spec]
  (prn :UNNOWN (spec-dispatch spec) spec)
  {})
