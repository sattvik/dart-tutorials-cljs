(ns helloworld
  (:require [cljs.nodejs :as node]))

(defn -main [& args]
  (node/enable-util-print!)
  (println "Hello, World!"))

(set! *main-cli-fn* -main)
