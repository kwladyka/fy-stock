(ns fy-stock.logs
  (:require [taoensso.timbre :as timbre]
            [sentry-clj.core :as sentry])
  (:import (java.net InetAddress)))

;; This code integrate Sentry with application in transparent way.
;; We can use only timbre for log purpose. Developers don't need to think about Sentry in the background.
;; The logger is https://github.com/ptaoussanis/timbre
;; We use appender to achieve it.

(def hostname (-> (InetAddress/getLocalHost)
                  (.getHostName)))

(def environment (not-empty (System/getenv "ENVIRONMENT")))

(def release nil)

(def sentry-dsn (not-empty (System/getenv "SENTRY_DSN")))

(def sentry-base (cond-> {:server-name hostname}
                         environment (assoc :environment environment)
                         release (assoc :release release)))

(def ^:private timbre->sentry-levels
  {:trace :debug
   :debug :debug
   :info :info
   :warn :warning
   :error :error
   :fatal :fatal
   :report :info})

(when sentry-dsn
  ;; Print sentry DSN when start app.
  ;; It is helpful during debugging to be sure it is configured.
  ;; For example SENTRY_DSN can be keep in vault and not pass to environment for some reason.
  (timbre/debug "sentry-dsn: " sentry-dsn)
  (sentry/init! sentry-dsn)

  (timbre/merge-config!
    {:appenders
     {:sentry
      {:enabled? true
       :async? true
       :min-level :info
       :rate-limit nil
       :output-fn :inherit
       :fn (fn [{:keys [level ?err msg_ ?ns-str context]}]
             (let [error-message (some-> ?err (.getLocalizedMessage))
                   causes (some-> ?err (ex-data) :causes)]
               (sentry/send-event (merge sentry-base
                                           {:level (get timbre->sentry-levels level)
                                            :fingerprint (when (or error-message (force msg_))
                                                           (->> (set [?ns-str error-message (force msg_)])
                                                                (clojure.set/union causes)
                                                                (remove #(= "" %))
                                                                (remove nil?)))
                                            :logger ?ns-str
                                            :extra context
                                            :message (not-empty (force msg_))
                                            :throwable ?err}))))}}}))

;; Ring middleware to catch exceptions and send to Sentry
(defn wrap-stacktrace
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Throwable ex
        (timbre/error ex)
        (throw ex)))))
