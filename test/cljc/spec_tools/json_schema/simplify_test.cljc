(ns spec-tools.json-schema.simplify-test
  (:require [clojure.test :refer [deftest are testing]]
            [spec-tools.json-schema.simplify :as simplify]))

(deftest test-simplify-all-of
  (are [x y] (= x (simplify/simplify-all-of y))
    {} {:allOf []}
    {:type "integer"} {:allOf [{:type "integer"}]}))

(deftest test-simplify-any-of
  (are [x y] (= x (simplify/simplify-any-of y))
    {} {:anyOf []}
    {:type ["string", "integer"]}
    {:anyOf [{:type "integer", "string"}]}))
