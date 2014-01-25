(ns clickme
  (:require [domina :as dom]
            [domina.events :as ev]))

(defn reverse-text []
  (let [node (dom/by-id "sample_text_id")
        text (dom/text node)]
    (dom/set-text! node (apply str (reverse text)))))

(doto (dom/by-id "sample_text_id")
  (dom/set-text! "Click me!")
  (ev/listen! :click (fn [_] (reverse-text))))
