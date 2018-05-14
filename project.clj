(defproject fy-stock "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [io.sentry/sentry-clj "0.5.0"]
                 [org.slf4j/slf4j-simple "1.7.25"]
                 [ring/ring-core "1.5.0"]
                 [http-kit "2.2.0"]
                 [ring-middleware-format "0.7.0"]
                 [bidi "2.1.3"]
                 [cheshire "5.8.0"]
                 [org.postgresql/postgresql "9.4.1207"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [clj-postgresql "0.7.0"]
                 [com.github.metaphor/lein-flyway "4.0.3"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler fy-stock.core/app}
  :main fy-stock.core
  :profiles {:dev {:dependencies [[ring/ring-mock "0.3.0"]]}})
