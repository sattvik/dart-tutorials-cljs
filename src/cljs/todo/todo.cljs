(ns todo
  (:require [domina :as d]
            [domina.events :as ev]))

(def to-do-input (d/by-id "to-do-input"))
(def to-do-list (d/by-id "to-do-list"))

(defn add-to-do-item [_]
  (let [li-element (d/html-to-dom (str "<li>" (d/value to-do-input) "</li>"))]
    (d/set-value! to-do-input "")
    (d/append! to-do-list li-element)))

(ev/listen! to-do-input :change add-to-do-item)
