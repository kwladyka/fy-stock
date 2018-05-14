(ns fy-stock.db.stock
  (:require [fy-stock.services.email :as email]))

(defn stock-level []
  )

(defn stock-changed []
  (email/send))

(defn receive-stock []
  (stock-changed))

(defn shipped->customer []
  (stock-changed))

(defn customer->return []
  (stock-changed))
