(ns slambook.server
  (:require [cljs.nodejs :as node]))

(def host "127.0.0.1")
(def port 4040)

(defn add-cors-headers
  [response]
  (dorun
    (map #(.setHeader response (name (key %)) (val %))
         {:Access-Control-Allow-Origin "*, "
          :Access-Control-Allow-Methods "POST, OPTIONS"
          :Access-Control-Allow-Headers "Origin, X-Requested-With, Content-Type, Accept"})))

(defmulti request-handler
  (fn [request response]
    (.-method request)))

(defn get-path [request]
  (let [url (js/require "url")]
    (-> request
        .-url
        ((fn [x] (.parse url x)))
        (aget "path"))))

(defmethod request-handler "POST"
  [request response]
  (println (str (.-method request) ": " (get-path request)))
  (add-cors-headers response)
  (.write response "Thanks for the data.  This is what I heard you say: ")
  (.setEncoding request "utf8")
  (let [data (atom "")]
    (doto request
      (.on "data"(fn [chunk] (swap! data str chunk)))
      (.on "end" #(doto response
                    (.write @data)
                    (.end))))))

(defmethod request-handler "OPTIONS"
  [request response]
  (add-cors-headers response)
  (println (str (.-method request) ": " (get-path request)))
  (set! (.-statusCode response) 204)
  (.end response))

(defmethod request-handler :default
  [request response]
  (add-cors-headers response)
  (set! (.-statusCode response) 404)
  (.write response
          (str "Not found: " (.-method request) ", " (get-path request)))
  (.end response))

(defn main [& args]
  (node/enable-util-print!)
  (let [http (js/require "http")
        server (.createServer http #(request-handler %1 %2))]
    (.listen server port host nil
             #(println (str "Listening on " host ":" port)))))

(set! *main-cli-fn* main)
