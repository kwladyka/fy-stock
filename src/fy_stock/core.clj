(ns fy-stock.core
  (:require [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [response not-found file-response]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.nested-params :refer [wrap-nested-params]]
            [bidi.ring :as bidi]
            [org.httpkit.server :as httpkit]
            [fy-stock.logs :as logs]))

(def route ["/" {"users/" {}
                 "products/" {}
                 "stock/" {}}])

(def handler-map
  {})

(def handler
  (bidi/make-handler route handler-map))

(def app
  (-> handler
      wrap-keyword-params
      wrap-nested-params
      wrap-params
      wrap-restful-format
      logs/wrap-stacktrace))

(defn -main []
  (httpkit/run-server app {:port 8080}))
