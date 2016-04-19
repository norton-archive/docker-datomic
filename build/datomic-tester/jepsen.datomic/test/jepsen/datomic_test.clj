(ns jepsen.datomic-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer [pprint]]
            [jepsen.datomic :refer :all]
            [jepsen
             [core :as jepsen]
             [report :as report]]))

(def datomic-version
  (or (System/getenv "DATOMIC_VERSION")
      "0.9.5350"))

(defn run-test!
  "Runs a test."
  [t]
  (let [test (jepsen/run! t)]
    (or (is (:valid? (:results test)))
        (println (:error (:results test))))))

(deftest da-partition
  (run-test! (da-partition-test datomic-version)))
