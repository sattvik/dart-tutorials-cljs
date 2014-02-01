(ns tute-milestone
  (:require [countdown.app :as app]))

(def milestone (atom nil))

(defn remove-milestone
  []
  (app/remove-milestone @milestone))

(js/Polymer "tute-milestone"
            #js {:removeMilestone remove-milestone
                 :milestone @milestone
                 :milestoneChanged #(reset! milestone %2)})
