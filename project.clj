(defmacro nodejs-config
  [name]
  `{:source-paths [~(str "src/cljs/" name)]
    :id ~name
    :compiler {:output-to ~(str "target/bin/" name ".js")
               :target :nodejs
               :optimizations :simple
               :pretty-print true}})

(defmacro web-config
  ([name]
   `(web-config ~name ~name))
  ([name js-name]
   `{:source-paths [~(str "src/cljs/" name)]
     :id ~name
     :compiler {:output-to ~(str "target/resources/public/" name "/" js-name ".js")
                :optimizations :simple
                :pretty-print true}}))

(defproject dart-tutorials-cljs "0.1.0-SNAPSHOT"
  :description "A Clojure port of the Dart tutorials"
  :url "https://github.com/sattvik/dart-tutorials-cljs"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2156"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [domina "1.0.2"]
                 [compojure "1.1.6"]]
  :plugins [[lein-cljsbuild "1.0.1"]
            [lein-ring "0.8.10"]]
  ;:hooks [leiningen.cljsbuild]
  :source-paths ["src/clj"]
  :resource-paths ["resources" "target/resources"]
  :cljsbuild {:builds [~(web-config "anagram")
                       ~(web-config "clickme")
                       {:source-paths ["src/cljs/countdown/tute_milestone/"
                                       "src/cljs/countdown/app/"]
                        :id "countdown"
                        :compiler {:output-to "target/resources/public/countdown/tute_milestone.js"
                                   :optimizations :simple
                                   :pretty-print true}}
                       {:source-paths ["src/cljs/countdown/tute_countdown/"
                                       "src/cljs/countdown/app/"]
                        :id "countdown"
                        :compiler {:output-to "target/resources/public/countdown/tute_countdown.js"
                                   :optimizations :simple
                                   :pretty-print true}}
                       ~(nodejs-config "dcat")
                       ~(nodejs-config "feet_wet_streams")
                       ~(nodejs-config "futures1")
                       ~(nodejs-config "futures2")
                       ~(nodejs-config "futures3")
                       ~(nodejs-config "futures4")
                       ~(nodejs-config "futures5")
                       ~(nodejs-config "futures5")
                       ~(nodejs-config "helloworld")
                       ~(web-config "its_all_about_you" "tute_its_all_about_you")
                       ~(web-config "mini")
                       ~(web-config "slambook" "tute_slambookform")
                       ~(nodejs-config "slambookserver")
                       ~(web-config "stopwatch" "tute_stopwatch")
                       ~(web-config "todo")
                       ~(web-config "todo_with_delete")]}
  :ring {:handler dart-tutorials.server/app
         :nrepl {:start? true}})
