(ns futures
  (:require [cljs.nodejs :as node]))

(def fs (node/require "fs"))

(defn do-something-with [content]
  (println "do something with content"))

(defn handle-error [err]
  (println "handle error"))

(defn print-daily-news-digest []
  (.readFile fs
             "resources/dailyNewsDigest.txt"
             (clj->js {:encoding "UTF-8"})
             (fn [err data]
               (if err
                 (handle-error err)
                 (do-something-with data)))))

(defn print-winning-lottery-numbers []
  (println "Winning lotto numbers: [23, 63, 87, 26, 2]"))

(defn print-weather-forecast []
  (println "Tomorrow's forecast: 70F, sunny"))

(defn print-baseball-score []
  (println "Baseball score: Red Sox 10, Yankees 0"))

(defn main []
  (node/enable-util-print!)
  (print-daily-news-digest)
  (print-winning-lottery-numbers)
  (print-weather-forecast)
  (print-baseball-score))

(set! *main-cli-fn* main)
