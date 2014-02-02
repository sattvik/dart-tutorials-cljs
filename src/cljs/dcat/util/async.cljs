(ns util.async
  (:require [cljs.core.async :refer [>! chan close!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn error? [e]
  (instance? js/Error e))

(defn throw-err [e]
  (if (error? e)
    (throw e)
    e))
