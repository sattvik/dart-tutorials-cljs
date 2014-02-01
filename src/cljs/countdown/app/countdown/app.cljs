(ns countdown.app
  (:require [cljs.core.async :as async :refer [>! <! chan close!]]
            [goog.date.DateTime])
  (:require-macros [cljs.core.async.macros :refer [alt! go go-loop]]))

(def indexed-db
  ((some-fn #(.-indexedDB %) #(.-webkitIndexedDB %) #(.-mozIndexedDB %))
   js/window))

(def has-indexed-db? (boolean indexed-db))

(defonce instance (atom {:hazMilestones false
                         :milestones []}))

(defn create-timer
  []
  (let [control (chan)
        ticks (chan (async/dropping-buffer 1))]
    (go-loop [state :stopped]
      (let [command (if (= :stopped state)
                      (<! control)
                      (alt! [control (async/timeout 1000)] ([v] v)))]
        (condp = command
          :start (recur :started)
          :stop  (recur :stopped)
          :quit  (do
                   (close! control)
                   (close! ticks))
          nil    (do
                   (>! ticks :tick)
                   (recur :started))
          (recur state))))
    {:control control
     :ticks ticks}))

(defonce timer (create-timer))

(defrecord Milestone [milestoneName happensOn timeRemainingAsString])

(defn duration->string
  [duration name]
  (let [days (quot duration 86400)
        hours (rem (quot duration 3600) 24)
        minutes (rem (quot duration 360) 60)
        seconds (rem duration 60)]
    (reduce str
            (conj (mapv #(if (zero? %1) "" (str %1 %2))
                        [days hours minutes seconds]
                        [" days, " " hours, " " minutes, " " seconds "])
                  (str "until " name)))))

(defn add-milestone
  [name occurs-on]
  (let [now-ms (.getTime (goog.date.DateTime.))
        occurs-ms (.getTime occurs-on)
        duration (quot (- occurs-ms now-ms) 1000)]
    (when (pos? duration)
      (swap! instance
             (fn [{:keys [milestones] :as state}]
               (assoc state
                      :hazMilestones true
                      :milestones (conj milestones (map->Milestone {:milestoneName name
                                                                    :happensOn occurs-on
                                                                    :timeRemainingAsString (duration->string duration name)})))))
      (go (>! (:control timer) :start)))))

(defn remove-milestone
  [milestone]
  (swap! instance
         (fn [{:keys [milestones] :as state}]
           (let [[chunk-a chunk-b] (split-with #(or (not= (aget milestone "milestoneName")
                                                          (aget % "milestoneName"))
                                                    (not= (aget milestone "happensOn")
                                                          (aget % "happensOn")))
                                               milestones)
                 new-milestones (apply vector (concat chunk-a (next chunk-b)))
                 have-milestones? (not (empty? milestones))]
             (when (not have-milestones?)
               (go (>! (:control timer) :stop)))
             (assoc state
                    :milestones new-milestones
                    :hazMilestones have-milestones?)))))

(defn clear-milestones
  []
  (swap! instance assoc :hazMilestones false :milestones [])
  (go (>! (:control timer) :stop)))

(defn tick
  [{:keys [happensOn milestoneName] :as milestone}]
  (let [now-ms (.getTime (goog.date.DateTime.))
        happens-ms (.getTime happensOn)
        duration (quot (- happens-ms now-ms) 1000)]
    (map->Milestone (assoc milestone :timeRemainingAsString (if (pos? duration)
                                                              (duration->string duration milestoneName)
                                                              (str "Huzzah for " milestoneName))))))

(defn tick-loop
  [timer]
  (go-loop []
    (when (<! timer)
      (swap! instance
             (fn [{:keys [milestones] :as state}]
               (assoc state :milestones (mapv tick milestones))))
      (recur))))

(defn start []
  (let [c (chan)]
    (go
      (if has-indexed-db?
        (do
          (when-not (empty? (:milestones @instance))
            (>! (:control timer) :start))
          (tick-loop (:ticks timer))
          (>! c {:status :ok
                 :value indexed-db}))
        (>! c {:status :error
               :message "IndexedDB not supported."}))
      (close! c))
    c))

(defn stop
  []
  (go (>! (:control timer) :quit)))
