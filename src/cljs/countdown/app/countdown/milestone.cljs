(ns countdown.milestone
  (:require [goog.date :as date]))

(defrecord Milestone [milestoneName happensOn timeRemainingAsString])

(defn- duration->string
  [duration]
  (let [days (quot duration 86400)
        hours (rem (quot duration 3600) 24)
        minutes (rem (quot duration 360) 60)
        seconds (rem duration 60)]
    (reduce str
            (conj (mapv #(if (zero? %1) "" (str %1 %2))
                        [days hours minutes]
                        [" days, " " hours, " " minutes, "])
                  (str seconds " seconds")))))

(defn format-time-remaining-string
  ([name happensOn]
   (let [duration (quot (- (.getTime happensOn) (.getTime (date/DateTime.))) 1000)]
     (if (pos? duration)
       (str (duration->string duration) " until " name)
       (str "Huzzah for " name \!))))
  ([{:keys [milestoneName happensOn]}]
   (format-time-remaining-string milestoneName happensOn)))

(defn create-milestone
  [name when]
  (->Milestone name when (format-time-remaining-string name when)))

(defn raw->Milestone
  [raw]
  (create-milestone (.-milestoneName raw)
                    (date/fromIsoString (.-happensOn raw))))

(defn Milestone->raw
  [{:keys [milestoneName happensOn]}]
  #js {:milestoneName milestoneName
       :happensOn (str happensOn)})
