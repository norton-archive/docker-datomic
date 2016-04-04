(ns jepsen.datomic
  (:require [clojure.tools.logging :refer :all]
            [datomic.api :only [q db] :as d]
            [clj-http.client :as http]
            [jepsen
             [db :as db]
             [checker :as checker]
             [client :as client]
             [control :as c]
             [generator :as gen]
             [os :as os]
             [tests :as tests]
             [util :refer [timeout]]]
            [knossos.model :as model]))

(defn da-setup-schema []
  (let [uri "datomic:sql://tester?jdbc:postgresql://postgres:5432/datomic?user=datomic&password=datomic"
        schema-tx [{:db/id #db/id[:db.part/db]
                    :db/ident :tester/register
                    :db/valueType :db.type/long
                    :db/cardinality :db.cardinality/one
                    :db/doc "A register for datomic tester"
                    :db.install/_attribute :db.part/db}]
        delete (d/delete-database uri)
        create (d/create-database uri)
        conn (d/connect uri)]
    (try
      (do
        ;; initialize schema
        @(d/transact conn schema-tx)
        ;; initialize register
        @(d/transact conn [[:db/add 1 :tester/register 0]])
        true)
      (finally ; release connection
        (d/release conn)))))

(defn da-r [node]
  (let [url (str "http://" (name node) ":8001/data/postgres/tester/-/entity?e=1")
        req {:headers {"Accept" "application/edn"}
             :as :clojure
             :throw-exceptions false}
        res (http/get url req)]
    res))

(defn da-w! [node value]
  (let [url (str "http://" (name node) ":8001/data/postgres/tester/")
        req {:headers {"Accept" "application/edn"}
             :content-type :application/edn
             :body (prn-str {:tx-data [[:db/add 1 :tester/register value]]})
             :as :clojure
             :throw-exceptions false}
        res (http/post url req)]
    res))

(defn da-cas! [node value new-value]
  (let [url (str "http://" (name node) ":8001/data/postgres/tester/")
        req {:headers {"Accept" "application/edn"}
             :content-type :application/edn
             :body (prn-str {:tx-data [[:db.fn/cas 1 :tester/register value new-value]]})
             :as :clojure
             :throw-exceptions false}
        res (http/post url req)]
    res))

(defn r   [_ _] {:type :invoke, :f :read, :value nil})
(defn w   [_ _] {:type :invoke, :f :write, :value (rand-int 5)})
(defn cas [_ _] {:type :invoke, :f :cas, :value [(rand-int 5) (rand-int 5)]})

(defn client
  "A client for a single compare-and-set register"
  [conn]
  (reify client/Client
    (setup! [_ test node]
      (client node)) ; NOTE: node is treated as the connection

    (invoke! [this test op]
      (timeout 15000 (assoc op :type :info, :error :timeout)
               (case (:f op)
                 :read (let [res (da-r conn)]
                         (if (= 200 (:status res))
                           (assoc op :type :ok :value (:tester/register (:body res)))
                           (assoc op :type :fail)))

                 :write (let [res (da-w! conn (:value op))]
                          (if (= 201 (:status res))
                            (assoc op :type :ok)
                            (assoc op :type :fail)))

                 :cas (let [[value new-value] (:value op)
                            res (da-cas! conn value new-value)]
                        (if (= 201 (:status res))
                          (assoc op :type :ok)
                          (assoc op :type :fail))))))

    (teardown! [_ test])))

(defn da-node-ids
  "Returns a map of node names to node ids."
  [test]
  (->> test
       :nodes
       (map-indexed (fn [i node] [node i]))
       (into {})))

(defn da-node-id
  "Given a test and a node name from that test, returns the ID for that node."
  [test node]
  ((da-node-ids test) node))

(defn db
  "Datomic DB for a particular version."
  [version]
  (reify db/DB
    (setup! [_ test node]
      (info node "db setup" version)
      (info node "id is" (da-node-id test node)))

    (teardown! [_ test node]
      (info node "db teardown"))

    db/Primary
    (setup-primary! [_ test node]
      (info node "db setup primary" version)
      (while
          (try
            (not (da-setup-schema))
            (catch Exception e
              (info node "db setup primary failed - retrying: " (.getMessage e))
              (Thread/sleep 500)
              true))))))

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
         :nodes ["n1"] ; TODO n1-n3
         :os os
         :db (db version)
         :client (client nil)
         :generator (->> (gen/mix [r w cas])
                         (gen/stagger 1)
                         (gen/clients)
                         (gen/time-limit 15))
         :model (model/cas-register 0)
         :checker checker/linearizable))
