
(require '[datomic.api :only [q db] :as d])

(defn da-setup-schema!
  [entity]
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
        @(d/transact conn [[:db/add entity :tester/register 0]])
        true)
      (finally ; release connection
        (d/release conn)))))

(do
  (da-setup-schema! 1) ; TBD 1 is hard-coded
  (System/exit 0))
