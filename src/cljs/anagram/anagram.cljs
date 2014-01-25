(ns anagram
  (:require [domina :as d]
            [domina.events :as ev]))

(def letter-counts
  {\a 9 \b 2 \c 2 \d 4 \e 12 \f 2 \g 3 \h 2 \i 9 \j 1 \k 1 \l 4 \m 2
   \n 6 \o 8 \p 2 \q 1 \r 6 \s 4 \t 6 \u 4 \v 2 \w 2 \x 1 \y 2 \z 1 \* 2})

(def scrabble-letters (mapcat (fn [[letter count]] (repeat count letter))
                              letter-counts))

(def letter-scores
  {\a 1 \b 3 \c 3 \d 2 \e 1 \f 4 \g 2 \h 4 \i 1 \j 8 \k 5 \l 1 \m 3
   \n 1 \o 1 \p 3 \q 10 \r 1 \s 1 \t 1 \u 1 \v 4 \w 4 \x 8 \y 4 \z 10 \* 0})

(def letter-pile (d/by-id "letterpile"))
(def result (d/by-id "result"))
(def value (d/by-id "value"))
(def clear-button (d/by-id "clearButton"))
(def score (atom 0))

(defn move-to
  [word-value letter score-fn dest]
  (let [letter-value (->> letter d/text first letter-scores)
        word-value (score-fn word-value letter-value)]
    (d/append! dest letter)
    (d/set-text! value word-value)
    word-value))

(defn move-letter [event]
  (let [letter (:target event)]
    (if (= (.-parentNode letter) letter-pile)
      (swap! score move-to letter + result)
      (swap! score move-to letter - letter-pile))))

(defn remove-letter
  [letters letter]
  (->> letters
       (split-with (fn [l] (not= l letter)))
       ((fn [[x y]]
         (concat x (drop 1 y))))))

(defn generate-new-letter
  [letters]
  (let [letter (rand-nth @letters)
        button (d/html-to-dom "<button>")]
    (swap! letters remove-letter letter)
    (doto button
      (d/add-class! "letter")
      (ev/listen! :click move-letter)
      (d/set-text! letter))
    (d/append! letter-pile button)))

(defn generate-new-letters []
  (reset! score 0)
  (d/set-text! value "")
  (let [letters (atom scrabble-letters)]
    (dorun (repeatedly 7 (partial generate-new-letter letters)))))

(defn new-letters []
  (d/destroy-children! letter-pile)
  (d/destroy-children! result)
  (generate-new-letters))

(defn main []
  (ev/listen! clear-button :click new-letters)
  (generate-new-letters))

(set! (.-onload js/window) main)
