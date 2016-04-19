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
             [nemesis :as nemesis]
             [tests :as tests]
             [util :refer [timeout]]]
            [jepsen.os.debian :as debian]
            [knossos.model :as model])
  (:import (java.net ConnectException)))

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
             :throw-exceptions false}]
    (try
      (http/get url req)
      (catch ConnectException e {:status 503}))))

(defn da-w! [node value]
  (let [url (str "http://" (name node) ":8001/data/postgres/tester/")
        req {:headers {"Accept" "application/edn"}
             :content-type :application/edn
             :body (prn-str {:tx-data [[:db/add 1 :tester/register value]]})
             :as :clojure
             :throw-exceptions false}]
    (try
      (http/post url req)
      (catch ConnectException e {:status 503}))))

(defn da-cas! [node value new-value]
  (let [url (str "http://" (name node) ":8001/data/postgres/tester/")
        req {:headers {"Accept" "application/edn"}
             :content-type :application/edn
             :body (prn-str {:tx-data [[:db.fn/cas 1 :tester/register value new-value]]})
             :as :clojure
             :throw-exceptions false}]
    (try
      (http/post url req)
      (catch ConnectException e {:status 503}))))

(defn wait-for-node
  [node timeout-secs color]
  (timeout (* 1000 timeout-secs)
           (throw (RuntimeException.
                   (str "Timed out after "
                        timeout-secs
                        " s waiting for peer recovery of "
                        node)))
           (loop []
             (when
                 (try
                   (not= 503 (:status (da-r node)))
                   (catch RuntimeException e true))
               (recur)))))

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
      (c/su (c/exec :killall :-9 :java))
      (wait-for-node node 20 :green)
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

(defn nemesis-pause
  ([process] (nemesis-pause rand-nth process))
  ([targeter process]
   (nemesis/node-start-stopper targeter
                               (fn start [t n]
                                 (c/su (c/exec :killall :-s "STOP" process))
                                 [:paused process])
                               (fn stop [t n]
                                 (c/su (c/exec :killall :-s "CONT" process))
                                 [:resumed process]))))

(defn nemesis-crash
  ([process] (nemesis-crash rand-nth process))
  ([targeter process]
   (nemesis/node-start-stopper targeter
                               (fn start [t n]
                                 (c/su (c/exec :killall :-9 process))
                                 [:killed n])
                               (fn stop [t n]
                                 (wait-for-node n 20 :green)
                                 [:restarted n]))))

(defn da-test
  "Defaults for testing datomic."
  [version name opts]
  (merge tests/noop-test
         {:name (str "datomic-" name)
          :nodes ["n1" "n2"] ; TODO n1-n3
          :os debian/os
          :db (db version)
          :client (client nil)
          :generator (->> (gen/mix [r w cas])
                          (gen/stagger 1/10)
                          (gen/delay 1)
                          (gen/nemesis
                           (gen/seq (cycle [(gen/sleep 5)
                                            {:type :info, :f :start}
                                            (gen/sleep 5)
                                            {:type :info, :f :stop}])))
                          (gen/time-limit 60))
          :model (model/cas-register 0)
          :checker (checker/compose
                    {:perf (checker/perf)
                     :linear checker/linearizable})}
         opts))

(defn da-partition-test
  "Testing with network partitions."
  [version]
  (da-test version "partition" {:nemesis (nemesis/partition-random-halves)}))

(defn da-pause-test
  "Testing with node pauses."
  [version]
  (da-test version "pause" {:nemesis (nemesis-pause :java)}))

(defn da-crash-test
  "Testing with node crashes."
  [version]
  (da-test version "crash" {:nemesis (nemesis-crash :java)}))
