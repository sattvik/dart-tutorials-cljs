(ns tute-stopwatch
  (:require [cljs.core.async :refer [<!]]
            [domina :as d]
            [tute-stopwatch.timer :as t])
  (:require-macros [cljs.core.async.macros :refer [go]])) 

(def state (atom {}))

(defn attached []
  (this-as this
    (let [auto-nodes (.-$ this)
          stop (aget auto-nodes "stopButton")
          reset (aget auto-nodes "resetButton")]
      (d/set-attr! stop :disabled true)
      (d/set-attr! reset :disabled true)
      (this-as this
        (reset! state {:nodes {:start-button (aget auto-nodes "startButton")
                               :stop-button stop
                               :reset-button reset}
                       :this this})))))

(defn detached []
  (when-let [timer (:timer @state)]
    (t/stop timer)))

(defn handle-timer [timer]
  (go 
    (while (<! (:heartbeat timer))
      (let [pad     (fn [n] (if (> n 9) (str n) (str \0 n)))
            elapsed (/  (- (js/Date.) (:start @state)) 1000)
            minutes (if (>= elapsed 60) (int (/ elapsed 60)) 0)
            seconds (int (rem elapsed 60))
            text    (str (pad minutes) \: (pad seconds))
            this    (:this @state)]
        (set! (.-counter this) text)))))
  
(defn start []
  (let [timer (t/start-timer 1000)
        now   (js/Date.)
        nodes (:nodes @state)]
    (d/set-attr! (:start-button nodes) :disabled true)
    (d/set-attr! (:reset-button nodes) :disabled true)
    (d/remove-attr! (:stop-button nodes) :disabled)
    (swap! state merge {:timer timer
                        :start now})
    (handle-timer timer)))

(defn stop []
  (let [{:keys [nodes timer]} @state]
    (d/remove-attr! (:start-button nodes) :disabled)
    (d/remove-attr! (:reset-button nodes) :disabled)
    (d/set-attr! (:stop-button nodes) :disabled true)
    (t/stop timer)
    (swap! state dissoc :timer :start)))

(defn reset []
  (let [{:keys [this nodes]} @state]
    (set! (.-counter this) "00:00")
    (d/set-attr! (:reset-button nodes) :disabled true)))

(def callbacks
  (js-obj "attached" attached
          "detached" detached
          "start" start
          "stop" stop
          "reset" reset
          "counter" "00:00"))

(js/Polymer "tute-stopwatch" callbacks)
