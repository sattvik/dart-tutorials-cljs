(ns todo-with-delete
  (:require [domina :as d]
            [domina.events :as ev]))

(def to-do-input (d/by-id "to-do-input"))
(def to-do-list (d/by-id "to-do-list"))

(defn add-to-do-item [_]
  (let [li-element (first (d/html-to-dom (str "<li>" (d/value to-do-input) "</li>")))]
    (d/set-value! to-do-input "")
    (ev/listen! li-element :click (fn [_] (d/destroy! li-element)))
    (d/append! to-do-list li-element)))

(ev/listen! to-do-input :change add-to-do-item)
(ev/listen! (d/by-id "delete-all") :click (fn [_] (d/destroy-children! to-do-list)))
