(ns jepsen.datomic-test
  (:require [clojure.test :refer :all]
            [jepsen.datomic :as da]
            [jepsen.core :as jepsen]))

(def datomic-version
  (or (System/getenv "DATOMIC_VERSION")
      "0.9.5359"))

(defn run-test!
  "Runs a test."
  [t]
  (let [test (jepsen/run! t)]
    (or (is (:valid? (:results test)))
        (println (:error (:results test))))))

(deftest da-noop
  (run-test! (da/da-noop-test datomic-version)))

(deftest da-partition
  (run-test! (da/da-partition-test datomic-version)))

(deftest da-pause
  (run-test! (da/da-pause-test datomic-version)))

(deftest da-crash
  (run-test! (da/da-crash-test datomic-version)))

(deftest da-mix
  (run-test! (da/da-mix-test datomic-version)))
