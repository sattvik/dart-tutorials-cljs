(ns util.async.macros)

(defmacro <?
  [expr]
  `(util.async/throw-err (cljs.core.async/<! ~expr)))

(defmacro call!
  [[fun & args]]
  `(let [out# (cljs.core.async/chan)
         cb#  (fn [err# & results#]
                (cljs.core.async.macros/go
                  (if err#
                    (cljs.core.async/>! out# err#)
                    (if (= 1 (count results#))
                      (cljs.core.async/>! out# (first results#)) 
                      (cljs.core.async/>! out# results#)))
                  (cljs.core.async/close! out#)))]
     (~fun ~@args cb#)
     out#))
