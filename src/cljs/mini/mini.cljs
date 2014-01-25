(ns mini
  (:require [domina :as d]))

(d/set-text! (d/by-id "RipVanWinkle")
             "Wake up, sleepy head!")
