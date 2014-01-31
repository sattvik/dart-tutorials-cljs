(ns tute-slambookform
  (:require [domina :as d]
            [domina.events :as ev]
            [goog.net.XhrIo]))

(def favorite-things (atom {:kittens true
                            :raindrops false
                            :mittens true
                            :kettles false}))
(def the-data (atom {:firstName "mem"
                     :favoriteQuote "Enjoy all your meals."
                     :favoriteColor "#4169E1"
                     :birthday "1963-08-30"
                     :volume 11
                     :catOrDog "dog"
                     :music 2
                     :zombies true}))
(def server-response (atom ""))

(def xhr (goog.net.XhrIo.))

(defn submit-form
  [event detail target]
  (.preventDefault event)
  (.send xhr "http://localhost:4040" "POST" (pr-str @the-data)))

(defn on-data
  [event]
  (if (.isSuccess xhr)
    (reset! server-response (str "Server says: " (.getResponseText xhr)))
    (reset! server-response "Server connect failed")))

(defn reset-form
  [event _ _]
  (.preventDefault event)
  (reset! favorite-things {:kittens false
                           :raindrops false
                           :mittens false
                           :kettles false})
  (reset! the-data {:firstName ""
                    :favoriteQuote ""
                    :favoriteColor ""
                    :birthday "2013-01-01"
                    :volume 0
                    :catOrDog "cat"
                    :music 0
                    :zombies false})
  (reset! server-response "Data cleared."))

(defn cat-or-dog
  [_ _ target]
  (swap! the-data assoc :catOrDog (d/value target)))

(letfn [(on-attach []
          (let [this (js* "this")]
            (add-watch favorite-things
                       ::observe
                       #(let [clj (clj->js @favorite-things)
                              js  (aget this "favoriteThings")]
                          (when (not= clj js)
                            (aset this "favoriteThings" clj))))
            (add-watch the-data
                       ::observe
                       #(let [clj (clj->js @the-data)
                              js  (aget this "theData")]
                          (when (not= clj js)
                            (aset this "theData" clj))))
            (add-watch server-response
                       ::observe
                       #(let [clj @server-response
                              js  (.-serverResponse this)]
                          (set! (.-serverResponse this) clj))))
          (ev/listen! xhr :complete on-data))
        (server-response-changed [_ n]
          (reset! server-response n))]
  (js/Polymer "tute-slambook-form"
              #js {:theData (clj->js @the-data)
                   :favoriteThings (clj->js @favorite-things)
                   :serverResponse @server-response
                   :submitForm submit-form
                   :resetForm reset-form
                   :catOrDog cat-or-dog
                   :observe #js {:theData.firstName #(swap! the-data assoc :firstName %2)
                                 :theData.favoriteQuote #(swap! the-data assoc :favoriteQuote %2)
                                 :theData.favoriteColor #(swap! the-data assoc :favoriteColor %2)
                                 :theData.birthday #(swap! the-data assoc :birthday %2)
                                 :theData.volume #(swap! the-data assoc :volume %2)
                                 :theData.catOrDog #(swap! the-data assoc :catOrDog %2)
                                 :theData.music #(swap! the-data assoc :music %2)
                                 :theData.zombies #(swap! the-data assoc :zombies %2)
                                 :favoriteThings.kittens #(swap! favorite-things assoc :kittens %2)
                                 :favoriteThings.mittens #(swap! favorite-things assoc :mittens %2)
                                 :favoriteThings.randrops #(swap! favorite-things assoc :randrops %2)
                                 :favoriteThings.kettles #(swap! favorite-things assoc :kettles %2)}
                   :attached on-attach}))
