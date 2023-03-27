# Setup

Start datahike-server with this config and the same version of datahike like in this project's `deps.edn`:
```clojure
{:databases [{:store {:backend :file
                      :path "/tmp/dh-file"}
              :name "users"
              :keep-history? true
              :schema-flexibility :write}]
 :server {:port  4444
          :join? false
          :loglevel :trace
          :dev-mode true
          :token :securerandompassword}}
```
