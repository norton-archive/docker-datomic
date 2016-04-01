(ns jepsen.datomic
  (:require [clojure.tools.logging :refer :all]
            [jepsen
             [db :as db]
             [tests :as tests]
             [control :as c]
             [util :refer [timeout meh]]]
            [jepsen.control.net :as net]
            [jepsen.os :as os]
            [jepsen.os.debian :as debian]))

(defn db
  "Datomic DB for a particular version."
  [version]
  (reify db/DB
    (setup! [_ test node]
      (info node "db setup" version))

    (teardown! [_ test node]
      (info node "db teardown"))

    db/Primary
    (setup-primary! [_ test node]
      (info node "db setup primary" version)
      (c/exec "/root/drop-init-tester.sh"))))

(def os
  (reify os/OS
    (setup! [_ test node]
      (info node "os setup")
      ;; TODO
      ;;(debian/setup-hostfile!)
      ;;(meh (net/heal)))
      )

    (teardown! [_ test node]
      (info node "os teardown"))))

(defn da-test
  [version]
  (assoc tests/noop-test
         :nodes ["n1"] # TODO n1-n3
         :os os
         :db (db version)))
