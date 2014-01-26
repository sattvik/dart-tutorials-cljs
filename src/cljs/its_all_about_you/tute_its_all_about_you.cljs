(ns tute-its-all-about-you
  (:require [cljs.reader :as edn]
            [domina :as d])
  (:require-macros [its-all-about-you.macros :refer [observe]]))

(def favorite-number (atom ""))
(def value-of-pi (atom ""))
(def horror-scope (atom ""))
(def fav-one (atom ""))
(def fav-two (atom ""))
(def fav-three (atom ""))
(def chocolate (atom ""))

(def int-as-edn (atom ""))
(def double-as-edn (atom ""))
(def bool-as-edn (atom ""))
(def string-as-edn (atom ""))
(def list-as-edn (atom ""))
(def map-as-edn (atom ""))

(def nodes (atom nil))


(defn populate-from-edn []
  (let [edn-data-string "{:favorite-number 42
                          :value-of-pi 3.141592
                          :chocolate true
                          :horror-scope \"virgo\"
                          :favorite-things [\"raindrops\" \"whiskers\" \"mittens\"]}"
        edn-data        (edn/read-string edn-data-string)]
    (reset! favorite-number (:favorite-number edn-data))
    (reset! value-of-pi (:value-of-pi edn-data))
    (reset! horror-scope (:horror-scope edn-data))
    (reset! fav-one (nth (:favorite-things edn-data) 0))
    (reset! fav-two (nth (:favorite-things edn-data) 1))
    (reset! fav-three (nth (:favorite-things edn-data) 2))
    (if (:chocolate edn-data)
      (do
        (d/set-attr! (aget @nodes "lovechocolate") :checked)
        (d/remove-attr! (aget @nodes "noloveforchocolate") :checked))
      (do
        (d/set-attr! (aget @nodes "noloveforchocolate") :checked)
        (d/remove-attr! (aget @nodes "lovechocolate") :checked)))))

(defn show-edn
  ([]
   (let [fav-num (js/parseInt @favorite-number)
         pi (js/parseFloat @value-of-pi)
         element (aget @nodes "lovechocolate")
         choco (boolean (d/value element))
         fav-things [@fav-one @fav-two @fav-three]
         form-data {:favorite-number fav-num
                    :value-of-pi pi
                    :chocolate choco
                    :horror-scope @horror-scope
                    :favorite-things fav-things}]
     (reset! int-as-edn (pr-str fav-num))
     (reset! double-as-edn (pr-str pi))
     (reset! bool-as-edn (pr-str choco))
     (reset! string-as-edn (pr-str @horror-scope))
     (reset! list-as-edn (pr-str fav-things))
     (reset! map-as-edn (pr-str form-data))))
  ([_ _ _]
   (show-edn)))

(defn attached []
  (observe :favoriteNumber favorite-number)
  (observe :valueOfPi value-of-pi)
  (observe :horrorScope horror-scope)
  (observe :favOne fav-one)
  (observe :favTwo fav-two)
  (observe :favThree fav-three)
  (observe :intAsJson int-as-edn)
  (observe :doubleAsJson double-as-edn)
  (observe :boolAsJson bool-as-edn)
  (observe :stringAsJson string-as-edn)
  (observe :listAsJson list-as-edn)
  (observe :mapAsJson map-as-edn)
  (this-as me (reset! nodes (.-$ me)))
  (populate-from-edn)
  (show-edn))

(defn favorite-number-changed [_ v]
  (reset! favorite-number v))

(defn value-of-pi-changed [_ v]
  (reset! value-of-pi v))

(defn horror-scope-changed [_ v]
  (reset! horror-scope v))

(defn fav-one-changed [_ v]
  (reset! fav-one v))

(defn fav-two-changed [_ v]
  (reset! fav-two v))

(defn fav-three-changed [_ v]
  (reset! fav-three v))

(defn chocolate-changed [_ v]
  (reset! chocolate v))

(js/Polymer "tute-its-all-about-you"
            #js {:attached attached
                 :favoriteNumber ""
                 :favoriteNumberChanged favorite-number-changed
                 :valueOfPi ""
                 :valueOfPiChanged value-of-pi-changed
                 :horrorScope ""
                 :horrorScopeChanged horror-scope-changed
                 :favOne ""
                 :favOneChanged fav-one-changed
                 :favTwo ""
                 :favTwoChanged fav-two-changed
                 :favThree ""
                 :favThreeChanged fav-three-changed
                 :chocolate ""
                 :chocolateChanged chocolate-changed
                 :showJson show-edn
                 :intAsJson ""
                 :doubleAsJson ""
                 :boolAsJson ""
                 :stringAsJson ""
                 :listAsJson ""
                 :mapAsJson ""})
