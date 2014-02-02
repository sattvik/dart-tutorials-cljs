(ns countdown.app
  (:require [cljs.core.async :as async :refer [>! <! chan close! put!]]
            [countdown.db :as db]
            [countdown.messages :refer [make-error error?]]
            [countdown.milestone :as ms]
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

(defn add-milestone
  [name occurs-on]
  (let [c (chan)
        now-ms (.getTime (goog.date.DateTime.))
        occurs-ms (.getTime occurs-on)
        duration (quot (- occurs-ms now-ms) 1000)]
    (go
      (if (pos? duration)
        (let [add-result (<! (db/add-milestone (ms/create-milestone name occurs-on)))]
          (.log js/console "add result:" add-result)
          (if (error? add-result)
            (>! c add-result)
            (do
              (put! (:control timer) :start)
              (swap! instance
                     (fn [{:keys [milestones] :as state}]
                       (assoc state
                              :hazMilestones true
                              :milestones (conj milestones (:value add-result)))))
              (>! c add-result))))
        (>! c (make-error "Cannot add events in the past.")))
      (close! c))
    c))

(defn remove-milestone
  [milestone]
  (let [c (chan)]
    (go
      (let [result (<! (db/remove-milestone milestone))]
        (when-not (error? result)
          (swap! instance
                 (fn [{:keys [milestones] :as state}]
                   (let [new-milestones (filterv #(not= (.-milestoneName %)
                                                        (.-milestoneName milestone))
                                                 milestones)
                         have-milestones? (not (empty? new-milestones))]
                     (when (not have-milestones?)
                       (put! (:control timer) :stop))
                     (assoc state
                            :milestones new-milestones
                            :hazMilestones have-milestones?)))))
        (>! c result)
        (close! c)))
    c))

(defn clear-milestones
  []
  (let [c (chan)]
    (go
      (let [result (<! (db/clear-milestones))]
        (when-not (error? result)
          (swap! instance assoc :hazMilestones false :milestones [])
          (>! (:control timer) :stop))
        (>! c result)
        (close! c)))
    c))


(defn tick
  [{:keys [happensOn milestoneName] :as milestone}]
  (let [now-ms (.getTime (goog.date.DateTime.))
        happens-ms (.getTime happensOn)
        duration (quot (- happens-ms now-ms) 1000)]
    (ms/map->Milestone (assoc milestone :timeRemainingAsString (ms/format-time-remaining-string milestone)))))

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
        (let [open-result (<! (db/open))]
          (.log js/console "open result:" (pr-str open-result))
          (when-not (error? open-result)
            (swap! instance
                   assoc
                   :milestones (:value open-result)
                   :hazMilestones (not (empty? (:value open-result))))
            (when-not (empty? (:milestones @instance))
              (>! (:control timer) :start))
            (tick-loop (:ticks timer)))
          (>! c open-result))
        (>! c (make-error  "IndexedDB not supported.")))
      (close! c))
    c))

(defn stop
  []
  (put! (:control timer) :quit)
  (db/close))
