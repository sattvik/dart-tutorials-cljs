(ns tute-countdown
  (:require [cljs.core.async :refer [<!]]
            [goog.date.DateTime :as date-time]
            [goog.dom :as dom]
            [countdown.app :as app]
            [countdown.messages :refer [error?]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def new-milestone-name (atom "New Year's Day"))
(def new-milestone-date (atom "2015-01-01"))
(def new-milestone-time (atom "00:00"))
(def error-message (atom ""))

(defn on-attach
  []
  (let [this (js* "this")]
    (add-watch new-milestone-name
               ::observe
               #(let [clj @new-milestone-name
                      js  (.-newMilestoneName this)]
                  (when (not= clj js)
                    (set! (.-newMilestoneName this) clj))))
    (add-watch new-milestone-date
                              ::observe
               #(let [clj @new-milestone-date
                      js  (.-newMilestoneDate this)]
                  (when (not= clj js)
                    (set! (.-newMilestoneDate this) clj))))
    (add-watch new-milestone-time
               ::observe
               #(let [clj @new-milestone-time
                      js  (.-newMilestoneTime this)]
                  (when (not= clj js)
                    (set! (.-newMilestoneTime this) clj))))
    (add-watch error-message
               ::observe
               #(let [clj @error-message
                      js  (.-errorMsg this)]
                  (when (not= clj js)
                    (set! (.-errorMsg this) clj))))
    (add-watch app/instance
               ::observe
               #(let [clj (clj->js @app/instance)
                      js  (aget this "appObj")]
                  (when (not= clj js)
                    (aset this "appObj" clj))))
    (let [start-status (app/start)]
      (go
        (let [result (<! start-status)]
          (if (error? result)
            (do
              (dorun
                (map #(set! (.-disabled (aget (.-$ this) %)) true)
                     ["addbutton" "clearbutton"]))
              (reset! error-message (:message result)))
            (reset! error-message "")))))))

(defn on-detach []
  (app/stop))

(defn add-milestone
  [event detail sender]
  (go
    (let [when (date-time/fromRfc822String (str @new-milestone-date " " @new-milestone-time))
          result (<! (app/add-milestone @new-milestone-name when))]
    (if (error? result)
      (reset! error-message (:message result))
      (reset! error-message "")))))

(defn clear
  []
  (go
    (let [result (<! (app/clear-milestones))]
      (if (error? result)
        (reset! error-message (:message result))
        (reset! error-message "")))))

(js/Polymer "tute-count-down"
            #js {:newMilestoneName @new-milestone-name
                 :newMilestoneNameChanged #(reset! new-milestone-name %2)
                 :newMilestoneDate @new-milestone-date
                 :newMilestoneDateChanged #(reset! new-milestone-date %2)
                 :newMilestoneTime @new-milestone-time
                 :newMilestoneTimeChanged #(reset! new-milestone-time %2)
                 :errorMsg @error-message
                 :errorMsgChanged #(reset! error-message %2)
                 :attached on-attach
                 :detached on-detach
                 :addMilestone add-milestone
                 :appObj (clj->js @app/instance)
                 :clear clear})
