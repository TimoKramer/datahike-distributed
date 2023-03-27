(ns datahike-distributed.core
  (:require [datahike.api :as d]
            [datahike-server-transactor.core]
            [taoensso.timbre :as log]))

(log/set-level! :trace)

(def distributed-cfg1 {:store {:backend :file
                               :path "/tmp/dh-file"}
                       :name "users"
                       :keep-history? true
                       :schema-flexibility :write
                       :writer {:backend :datahike-server
                                :client-config  {:timeout 300
                                                 :endpoint "http://localhost:3333"
                                                 :db-name "users"}}})

(comment
  (d/create-database distributed-cfg1)
  (d/delete-database distributed-cfg1)
  (def conn (d/connect distributed-cfg1))
  (d/release conn))

(d/transact conn [[:db/add -1 :name "bar"]])
(d/transact conn [[:db/add -1 :name "foo"]])

(d/transact conn [{:db/ident :name
                   :db/valueType :db.type/string
                   :db/cardinality :db.cardinality/one}
                  {:db/ident :age
                   :db/valueType :db.type/long
                   :db/cardinality :db.cardinality/one}])

(d/q {:query '[:find ?name
               :where [_ :name ?name]]}
     @conn) 

