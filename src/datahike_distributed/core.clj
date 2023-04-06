(ns datahike-distributed.core
  (:require [datahike.api :as d]
            [datahike-server-transactor.core]
            [taoensso.timbre :as log]))

(log/set-level! :trace)

(def distributed-cfg1 {:store {:backend :file
                               :scope "test.lambdaforge.net"
                               :path "/tmp/users"}
                       :keep-history? true
                       :schema-flexibility :write
                       :writer {:backend :datahike-server
                                :client-config  {:timeout 300
                                                 :endpoint "http://localhost:3333"}}})

(comment
  ;; not supported client side yet
  (d/create-database distributed-cfg1)
  (d/delete-database distributed-cfg1))

(def conn (d/connect distributed-cfg1))

(d/transact conn [{:db/ident       :name
                   :db/valueType   :db.type/string
                   :db/cardinality :db.cardinality/one}
                  {:db/ident       :age
                   :db/valueType   :db.type/long
                   :db/cardinality :db.cardinality/one}])

(d/transact conn [[:db/add -1 :name "foo"]])

(d/q {:query '[:find ?name
               :where [_ :name ?name]]}
     @conn)

(d/release conn)
