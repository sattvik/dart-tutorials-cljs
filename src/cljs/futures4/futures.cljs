(ns futures
  (:require [cljs.core.async :refer [<! >! chan close!]]
            [cljs.nodejs :as node])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn do-something-with [value]
  (println value))

(defn future [function]
  (let [c (chan)]
    (go
      (>! c (function))
      (close! c))
    c))

(defn expensive-a []
  "from expensive-a")

(defn expensive-b []
  "from expensive-b")

(defn expensive-c []
  "from expensive-c")

(defn main []
  (go 
    (<! (future expensive-a))
    (<! (future expensive-b))
    (do-something-with (<! (future expensive-c))))
)

(set! *main-cli-fn* main)
