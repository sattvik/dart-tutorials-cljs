(defproject dart-tutorials-cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2138"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [domina "1.0.2"]
                 [compojure "1.1.6"]]
  :plugins [[lein-cljsbuild "1.0.1"]
            [lein-ring "0.8.10"]]
  ;:hooks [leiningen.cljsbuild]
  :source-paths ["src/clj"]
  :resource-paths ["resources" "target/resources"]
  :cljsbuild {:builds [{:source-paths ["src/cljs/anagram"]
                        :compiler {:output-to "target/resources/public/anagram/anagram.js"
                                   :optimizations :simple
                                   :pretty-print true}}
                       {:source-paths ["src/cljs/clickme"]
                        :compiler {:output-to "target/resources/public/clickme/clickme.js"
                                   :optimizations :simple
                                   :pretty-print true}}
                       {:source-paths ["src/cljs/mini"]
                        :compiler {:output-to "target/resources/public/mini/mini.js"
                                   :optimizations :simple
                                   :pretty-print true}}
                       {:source-paths ["src/cljs/stopwatch"]
                        :compiler {:output-to "target/resources/public/stopwatch/tute_stopwatch.js"
                                   :optimizations :simple
                                   :pretty-print true}}
                       {:source-paths ["src/cljs/todo"]
                        :compiler {:output-to "target/resources/public/todo/todo.js"
                                   :optimizations :simple
                                   :pretty-print true}}
                       {:source-paths ["src/cljs/todo_with_delete"]
                        :compiler {:output-to "target/resources/public/todo_with_delete/todo_with_delete.js"
                                   :optimizations :simple
                                   :pretty-print true}}]}
  :ring {:handler dart-tutorials.server/app
         :nrepl {:start? true}})
