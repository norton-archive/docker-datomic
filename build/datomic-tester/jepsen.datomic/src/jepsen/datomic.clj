(ns jepsen.datomic
  (:require [jepsen.tests :as tests]))

(defn da-test
  [version]
  (assoc tests/noop-test
         :nodes ["n1" "n2" "n3"]))
