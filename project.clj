(defproject metosin/spec-tools "0.1.0-SNAPSHOT"
  :description "Common utilities for clojure.spec"
  :url "https://github.com/metosin/spec-tools"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha12"]
                 [clj-time "0.12.0"]]
  :plugins [[funcool/codeina "0.4.0"]]

  :codeina {:target "doc"
            :src-uri "http://github.com/metosin/spec-tools/blob/master/"
            :src-uri-prefix "#L"}

  :profiles {:dev {:plugins [[jonase/eastwood "0.2.3"]]
                   :dependencies [[criterium "0.4.4"]
                                  [prismatic/schema "1.1.3"]
                                  [org.clojure/test.check "0.9.0"]
                                  [org.clojure/clojurescript "1.9.227"]]}
             :perf {:jvm-opts ^:replace ["-server"]}}
  :aliases {"all" ["with-profile" "dev"]
            "perf" ["with-profile" "default,dev,perf"]
            "test-clj" ["all" "do" ["test"] ["check"]]})
