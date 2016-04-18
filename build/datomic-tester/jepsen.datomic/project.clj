(defproject jepsen.datomic "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-exec "0.3.6"]]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [clojure-complete/clojure-complete "0.2.4"]
                 [com.datomic/datomic-pro "0.9.5350"
                  :exclusions [org.slf4j/slf4j-nop org.slf4j/log4j-over-slf4j]]
                 [org.slf4j/slf4j-log4j12 "1.7.21"]
                 [org.postgresql/postgresql "9.4.1208"]
                 [org.apache.httpcomponents/httpclient "4.5.2"]
                 [clj-http "3.0.1"]
                 [jepsen "0.1.0"]])
