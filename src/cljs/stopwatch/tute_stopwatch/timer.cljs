(ns tute-stopwatch.timer
  (:require [cljs.core.async :refer [>! chan close! put! timeout]])
  (:require-macros [cljs.core.async.macros :refer [go alts!]]))

(defn start-timer
  [duration]
  (let [heartbeat (chan)
        control (chan)
        continue (atom true)]
    (go
      (while @continue
        (let [[quit _] (alts! [control (timeout duration)])]
          (if quit
            (do
              (close! control)
              (close! heartbeat)
              (reset! continue false))
            (>! heartbeat :tick)))))
    {:heartbeat heartbeat
     :control control}))

(defn stop [timer]
  (put! (:control timer) :stop))
