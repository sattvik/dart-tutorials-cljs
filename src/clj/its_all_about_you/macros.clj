(ns its-all-about-you.macros)

(defmacro observe
  [property aref]
  `(let [this# (~'js* "this")
         set-prop# (fn [val#]
                     (set! (~(symbol (str ".-" (name property))) this#)
                        (str val#)))]
     (add-watch ~aref ::observe #(set-prop# %4))
     (set-prop# (deref ~aref))))
