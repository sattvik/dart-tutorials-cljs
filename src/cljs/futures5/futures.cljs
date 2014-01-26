(ns futures
  (:require [cljs.core.async :refer [<! >! chan close!] :as async]
            [cljs.nodejs :as node])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn choose-best-response [vals]
  (nth vals 1))

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
    (let [futures [(future expensive-a)
                   (future expensive-b)
                   (future expensive-c)]
          results (async/map vector futures)]
      (-> (<! results)
          choose-best-response
          do-something-with))))

(set! *main-cli-fn* main)
