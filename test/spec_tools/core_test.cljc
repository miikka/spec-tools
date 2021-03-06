(ns spec-tools.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.spec :as s]
            [spec-tools.core :as st]
            [clojure.string :as str]))

(s/def ::age (s/and st/integer? #(> % 10)))
(s/def ::over-a-million (s/and st/int? #(> % 1000000)))
(s/def ::lat st/double?)
(s/def ::language (s/and st/keyword? #{:clojure :clojurescript}))
(s/def ::truth st/boolean?)
(s/def ::uuid st/uuid?)
(s/def ::birthdate st/inst?)

(deftest spec-tools-conform-test
  (testing "in default mode"
    (testing "nothing is conformed"
      (is (= ::s/invalid (st/conform ::age "12")))
      (is (= ::s/invalid (st/conform ::over-a-million "1234567")))
      (is (= ::s/invalid (st/conform ::lat "23.1234")))
      (is (= ::s/invalid (st/conform ::language "clojure")))
      (is (= ::s/invalid (st/conform ::truth "false")))
      (is (= ::s/invalid (st/conform ::uuid "07dbf30f-c99e-4e5d-b76e-5cbdac3b381e")))
      (is (= ::s/invalid (st/conform ::birthdate "2006-01-02T15:04:05.999999-07:00")))))

  (testing "string-conformers"
    (let [conform #(st/conform %1 %2 st/string-conformations)]
      (testing "everything gets conformed"
        (is (= 12 (conform ::age "12")))
        (is (= 1234567 (conform ::over-a-million "1234567")))
        (is (= 23.1234 (conform ::lat "23.1234")))
        (is (= false (conform ::truth "false")))
        (is (= :clojure (conform ::language "clojure")))
        (is (= #uuid "07dbf30f-c99e-4e5d-b76e-5cbdac3b381e"
               (conform ::uuid "07dbf30f-c99e-4e5d-b76e-5cbdac3b381e")))
        (is (= #inst "2006-01-02T15:04:05.999999-07:00"
               (conform ::birthdate "2006-01-02T15:04:05.999999-07:00"))))))

  (testing "json-conformers"
    (let [conform #(st/conform %1 %2 st/json-conformations)]
      (testing "some are not conformed"
        (is (= ::s/invalid (conform ::age "12")))
        (is (= ::s/invalid (conform ::over-a-million "1234567")))
        (is (= ::s/invalid (conform ::lat "23.1234")))
        (is (= ::s/invalid (conform ::truth "false"))))
      (testing "some are conformed"
        (is (= :clojure (conform ::language "clojure")))
        (is (= #uuid "07dbf30f-c99e-4e5d-b76e-5cbdac3b381e"
               (conform ::uuid "07dbf30f-c99e-4e5d-b76e-5cbdac3b381e")))
        (is (= #inst "2006-01-02T15:04:05.999999-07:00"
               (conform ::birthdate "2006-01-02T15:04:05.999999-07:00")))))))

(deftest unform-test
  (let [unform-conform #(s/unform %1 (st/conform %1 %2 st/string-conformations))]
    (testing "conformed values can be unformed"
      (is (= 12 (unform-conform ::age "12")))
      (is (= 1234567 (unform-conform ::age "1234567")))
      (is (= 23.1234 (unform-conform ::lat "23.1234")))
      (is (= false (unform-conform ::truth "false")))
      (is (= :clojure (unform-conform ::language "clojure")))
      (is (= #uuid "07dbf30f-c99e-4e5d-b76e-5cbdac3b381e"
             (unform-conform ::uuid "07dbf30f-c99e-4e5d-b76e-5cbdac3b381e")))
      (is (= #inst "2006-01-02T15:04:05.999999-07:00"
             (unform-conform ::birthdate "2006-01-02T15:04:05.999999-07:00"))))))

(deftest extending-test
  (let [my-conformations (-> st/string-conformations
                          (assoc
                            keyword?
                            (comp
                              keyword
                              str/reverse
                              str/upper-case)))]
    (testing "string-conformers"
      (is (= :kikka (st/conform st/keyword? "kikka" st/string-conformations))))
    (testing "my-conformers"
      (is (= :AKKIK (st/conform st/keyword? "kikka" my-conformations))))))
