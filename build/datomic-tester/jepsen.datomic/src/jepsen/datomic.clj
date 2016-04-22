(ns jepsen.datomic
  (:require [clojure.tools.logging :refer :all]
            [clojure.pprint :as pprint]
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
            [jepsen.checker.timeline :as timeline]
            [jepsen.os.debian :as debian]
            [knossos.model :as model])
  (:import java.net.ConnectException
           java.net.SocketException
           org.apache.http.NoHttpResponseException))

(defn node-ids
  "Returns a map of node names to node ids."
  [test]
  (->> test
       :nodes
       (map-indexed (fn [i node] [node i]))
       (into {})))

(defn node-id
  "Given a test and a node name from that test, returns the ID for that node."
  [test node]
  ((node-ids test) node))

(defn first-node
  [test]
  (first (:nodes test)))

(defn first-node-id
  [test]
  (node-id test (first-node test)))

(defn test-entity
  [test]
  1)

(defn da-setup-schema
  [test node]
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
        @(d/transact conn [[:db/add (test-entity test) :tester/register 0]])
        true)
      (finally ; release connection
        (d/release conn)
        (d/shutdown false)))))

(defn da-read
  [test node]
  (let [url (str "http://" (name node) ":8001/data/postgres/tester/-/entity?e=" (test-entity test))
        req {:headers {"Accept" "application/edn"}
             :as :clojure
             ;; DEBUG :save-request? true :debug-body true
             :throw-exceptions false}]
    (try
      (let [res (http/get url req)]
        ;; DEBUG (pprint/pprint [url req res])
        res)
      (catch ConnectException e {:status :ConnectException})
      (catch SocketException e {:status :SocketException})
      (catch NoHttpResponseException e {:status :NoHttpResponseException}))))

(defn da-write!
  [test node value]
  (let [url (str "http://" (name node) ":8001/data/postgres/tester/")
        req {:headers {"Accept" "application/edn"}
             :content-type :application/edn
             :body (prn-str {:tx-data [[:db/add (test-entity test) :tester/register value]]})
             :as :clojure
             ;; DEBUG :save-request? true :debug-body true
             :throw-exceptions false}]
    (try
      (let [res (http/post url req)]
        ;; DEBUG (pprint/pprint [url req res])
        res)
      (catch ConnectException e {:status :ConnectException})
      (catch SocketException e {:status :SocketException})
      (catch NoHttpResponseException e {:status :NoHttpResponseException}))))

(defn da-cas!
  [test node value new-value]
  (let [url (str "http://" (name node) ":8001/data/postgres/tester/")
        req {:headers {"Accept" "application/edn"}
             :content-type :application/edn
             :body (prn-str {:tx-data [[:db.fn/cas (test-entity test) :tester/register value new-value]]})
             :as :clojure
             ;; DEBUG :save-request? true :debug-body true
             :throw-exceptions false}]
    (try
      (let [res (http/post url req)]
        ;; DEBUG (pprint/pprint [url req res])
        res)
      (catch ConnectException e {:status :ConnectException})
      (catch SocketException e {:status :SocketException})
      (catch NoHttpResponseException e {:status :NoHttpResponseException}))))

(defn member?
  [elt col]
  (some #(= elt %) col))

(defn wait-for-node
  [test node timeout-secs & expected-statuses]
  (timeout (* 1000 timeout-secs)
           (throw (RuntimeException.
                   (str "Timed out after "
                        timeout-secs
                        " s waiting for peer recovery of "
                        node)))
           (do
             (loop []
               (when
                   (try
                     (let [status (:status (da-read test node))
                           member (not (member? status expected-statuses))]
                       (if member
                         (Thread/sleep 100))
                       member)
                     (catch RuntimeException e true))
                 (recur))))))

(defn client
  "A client for a single compare-and-set register"
  [conn]
  (reify client/Client
    (setup! [_ test node]
      (client node)) ; NOTE: node is treated as the connection

    (invoke! [this test op]
      (timeout 15000 (assoc op :type :info, :error :timeout)
               (case (:f op)
                 :read (let [res (da-read test conn)
                             status (:status res)]
                         (if (= 200 status)
                           (assoc op :type :ok :value (:tester/register (:body res)))
                           (assoc op :type :fail :value res)))

                 :write (let [res (da-write! test conn (:value op))
                              status (:status res)]
                          (if (= 201 status)
                            (assoc op :type :ok)
                            (assoc op :type :fail :value res)))

                 :cas (let [[value new-value] (:value op)
                            res (da-cas! test conn value new-value)
                            status (:status res)]
                        (if (= 201 status)
                          (assoc op :type :ok)
                          (assoc op :type :fail :value res))))))

    (teardown! [_ test])))

(defn db
  "Datomic DB for a particular version."
  [version]
  (reify db/DB
    (setup! [_ test node]
      (info node "db setup" version)
      (c/su (c/exec :killall :-9 :java))
      (Thread/sleep 100)
      (wait-for-node test node 60 200 404)
      (info node "id is" (node-id test node)))

    (teardown! [_ test node]
      (info node "db teardown"))

    db/Primary
    (setup-primary! [_ test node]
      (info node "db setup primary" version)
      (da-setup-schema test node)
      (Thread/sleep 1000))))

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
                                 (Thread/sleep 100)
                                 [:killed n])
                               (fn stop [t n]
                                 (wait-for-node t n 30 200)
                                 [:restarted n]))))

(defn gen-sleep
  ([]
   (gen-sleep 5))
  ([n]
   (gen/sleep n))
  ([min max]
   {:pre (> max min)}
   (let [n (rand-int (- max min))
         m (+ min n)]
     (gen/sleep m))))

(defn da-create-test
  "Defaults for testing datomic."
  [version name opts]
  (merge tests/noop-test
         {:name (str "datomic-" name)
          :nodes ["n1" "n2"] ; TODO n1-n3
          :os debian/os
          :db (db version)
          :client (client nil)
          :model (model/cas-register 0)
          :checker (checker/compose
                    {:linear checker/linearizable
                     :perf (checker/perf)
                     :timeline timeline/html})}
         opts))

(defn da-noop-test
  "Testing with noop nemesis."
  [version]
  (da-create-test version "noop" {:generator (->> gen/cas
                                                  (gen/stagger 1/10)
                                                  (gen/clients)
                                                  (gen/time-limit 60))}))

(defn da-create-test-nemesis
  [version name nemesis]
  (da-create-test version name {:nemesis nemesis
                                :generator (->> gen/cas
                                                (gen/stagger 1/10)
                                                (gen/nemesis (gen/seq (cycle [(gen-sleep 5)
                                                                              {:type :info, :f :start}
                                                                              (gen-sleep 1 5)
                                                                              {:type :info, :f :stop}])))
                                                (gen/time-limit 60))}))

(defn da-partition-test
  "Testing with network partitions."
  [version]
  (da-create-test-nemesis version "partition" (nemesis/partition-random-halves)))

(defn da-pause-test
  "Testing with node pauses."
  [version]
  (da-create-test-nemesis version "pause" (nemesis-pause :java)))

(defn da-crash-test
  "Testing with node crashes."
  [version]
  (da-create-test-nemesis version "crash" (nemesis-crash :java)))

(defn da-mix-test
  "Testing with network partitions, node pauses, and node crashes."
  [version]
  (da-create-test version "mix"
                  {:nemesis (nemesis/compose
                             {{:partition-start :start
                               :partition-stop :stop} (nemesis/partition-random-halves)
                              {:pause-start :start
                               :pause-stop :stop} (nemesis-pause :java)
                              {:crash-start :start
                               :crash-stop :stop} (nemesis-crash :java)
                              })
                   :generator (->> gen/cas
                                   (gen/stagger 1/10)
                                   (gen/nemesis
                                    (->> (gen/mix [(gen/seq [(gen-sleep 5)
                                                             {:type :info, :f :partition-start}
                                                             (gen-sleep 1 5)
                                                             {:type :info, :f :partition-stop}])
                                                   (gen/seq [(gen-sleep 5)
                                                             {:type :info, :f :pause-start}
                                                             (gen-sleep 1 5)
                                                             {:type :info, :f :pause-stop}])
                                                   (gen/seq [(gen-sleep 5)
                                                             {:type :info, :f :crash-start}
                                                             (gen-sleep 1 5)
                                                             {:type :info, :f :crash-stop}])])
                                         (gen/time-limit 50)))
                                   (gen/time-limit 60))}))
