(ns helloworld)

(defn -main [& args]
  (println "Hello, World!"))

(set! *main-cli-fn* -main)
