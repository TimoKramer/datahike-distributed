(ns dev.user
  (:require [datahike.api :as d]
            [datahike-server-transactor.core]
            [taoensso.timbre :as log]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(log/set-level! :trace)

(def distributed-cfg (-> (io/resource "config.edn")
                         slurp
                         edn/read-string))

(comment
  ;; not supported client side yet
  (d/create-database distributed-cfg)
  (d/delete-database distributed-cfg)

  (def conn (d/connect distributed-cfg))

  (d/transact conn [{:db/ident       :name
                     :db/valueType   :db.type/string
                     :db/cardinality :db.cardinality/one
                     :db/unique      :db.unique/identity}
                    {:db/ident       :age
                     :db/valueType   :db.type/long
                     :db/cardinality :db.cardinality/one}])

  (d/transact conn [[:db/add "foo" :name "foo"]
                    [:db/add "foo" :age 2]])

  (d/q {:query '[:find (pull ?e [*])
                 :where
                 [?e :name ?name]]}
       @conn)

  (d/release conn))
