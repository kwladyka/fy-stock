(ns fy-stock.users
  (:require [fy-stock.services.google :as google]
            [fy-stock.services.fb :as fb]))

(defn login [auth-system token]
  (case auth-system
    :google (google/login token)
    :fb (fb/login token)))

(defn logout [auth-system token]
  (case auth-system
    :google (google/logout token)
    :fb (fb/logout token)))

(defn auth-system->user-data [auth-system token]
  (case auth-system
    :google (google/get-user token)
    :fb (fb/get-user token)))

;; Assumption is user data are in centralised DB.
;; User have to be registered in e-store to sell.

(defn e-store->user-data [email]
  )
