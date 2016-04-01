(require '[datomic.api :only [q db] :as d])

(def uri "datomic:sql://datomic-tester?jdbc:postgresql://postgres:5432/datomic?user=datomic&password=datomic")

(d/create-database uri)

(def conn (d/connect uri))

(def schema-tx
  [{:db/id #db/id[:db.part/db]
    :db/ident :datomic-tester/register
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "A register for datomic tester"
    :db.install/_attribute :db.part/db}])

;; submit schema transaction
@(d/transact conn schema-tx)

(System/exit 0)
