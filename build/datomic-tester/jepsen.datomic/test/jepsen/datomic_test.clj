(ns jepsen.datomic-test
  (:require [clojure.test :refer :all]
            [jepsen.core :as jepsen]
            [jepsen.datomic :as da]))

(def datomic-version
  (or (System/getenv "DATOMIC_VERSION")
      "0.9.5350"))

(deftest da-test
  (is (:valid? (:results (jepsen/run! (da/da-test datomic-version))))))
