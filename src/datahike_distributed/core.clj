(ns datahike-distributed.core
  (:gen-class)
  (:require [datahike.api :as d]
            [datahike-server-transactor.core]
            [taoensso.timbre :as log]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [tea-time.core :as tt]))

(log/set-level! :debug)

(def distributed-cfg (-> (io/resource "config.edn")
                         slurp
                         edn/read-string))

(def conn (d/connect distributed-cfg))

(tt/start!)

(def ^:dynamic *tt* nil)

(defn -main []
  (->> (tt/every! 10 2 (bound-fn [] (->> (d/q {:query '[:find (pull ?e [*])
                                                        :where
                                                        [?e :name ?name]]}
                                            @conn)
                                         (log/info "Result: "))))
       (alter-var-root (var *tt*))))

(comment
  (-main)
  (tt/cancel! *tt*))
