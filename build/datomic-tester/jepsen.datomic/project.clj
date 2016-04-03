(defproject jepsen.datomic "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-exec "0.3.6"]]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.nrepl "0.2.10"]
                 [clojure-complete/clojure-complete "0.2.3"]
                 [com.datomic/datomic-pro "0.9.5350"
                  :exclusions [org.slf4j/slf4j-nop org.slf4j/log4j-over-slf4j]]
                 [org.slf4j/slf4j-log4j12 "1.6.4"]
                 [org.postgresql/postgresql "9.3-1102-jdbc41"]
                 [org.apache.httpcomponents/httpclient "4.3.5"]
                 [clj-http "2.1.0"]
                 [jepsen "0.0.9"]])
