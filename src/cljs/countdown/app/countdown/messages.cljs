(ns countdown.messages)

(defn make-value
  [v]
  {:type :value
   :value v})

(defn make-error
  ([err]
   (if (string? err)
     {:type :error
      :message err
      :value err}
     {:type :error
      :message (.-message err)
      :value err}))
  ([err-msg value]
   {:type :error
    :message err-msg
    :value value}))

(defn error?
  [msg]
  (= :error (:type msg)))
