(ns dcat
  (:require [cljs.core.async :as async :refer [>! <! chan close!]]
            [cljs.nodejs :as node]
            [clojure.string :as str]
            [goog.string :as gstr])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [util.async.macros :refer [<? call!]]))

(def eol (.-EOL (js/require "os")))

(defn parse-args
  []
  (let [parsed (-> (js/require "optimist")
                   (.alias "n" "line-number")
                   (.boolean "n")
                   (.-argv))]
    {:files (js->clj (.-_ parsed))
     :number-lines?  (.-n parsed)}))

(defn handle-error
  [path]
  (let [fs (js/require "fs")]
    (.write (.-stderr node/process)
            (if (and (.existsSync fs path)
                     (.isDirectory (.statSync fs path)))
              (str "error: " path " is a directory" eol)
              (str "error: " path " not found" eol)))))

(defn file-reader
  [fd]
  (let [out (chan)
        fs (js/require "fs")
        buf-size 1024
        buffer (js/Buffer. buf-size)]
    (go-loop []
      (try
        (let [[bytes-read _] (<? (call! (.read fs fd buffer 0 buf-size nil)))]
          (when-not (zero? bytes-read)
            (let [return-buffer  (js/Buffer. bytes-read)]
              (.copy buffer return-buffer 0 0 bytes-read)
              (>! out return-buffer)
              (recur))))
        (catch js/Error e
          (>! out e)))
      (close! out))
    out))

(defn string-decoder
  [in]
  (let [out (chan)]
    (go-loop []
      (try
        (when-let [buf (<? in)]
          (>! out (.toString buf))
          (recur))
        (catch js/Error e
          (>! out e))) 
      (close! out))
    out))

(defn line-transform
  [in]
  (let [out (chan)]
    (go-loop [cur-str ""]
      (try
        (if-let [new-str (<? in)]
          (let [full-str (str cur-str new-str)
                split-up (str/split-lines full-str)
                ends-with-eol? (gstr/endsWith full-str eol)
                [to-put saved] (if ends-with-eol?
                                 [split-up ""]
                                 (-> (split-at (dec (count split-up)) split-up)
                                     ((fn [[a b]] [a (first b)]))))]
            (dorun (map #(async/put! out %) to-put))
            (recur saved))
          (when-not (empty? cur-str)
            (>! out cur-str)))
        (catch js/Error e
          (>! out e))) 
      (close! out))
    out))

(defn dcat
  [{:keys [files number-lines?]}]
  (if (empty? files)
    (doto (.-stdin node/process)
      (.resume)
      (.pipe (.-stdout node/process)))
    (let [fs (js/require "fs") 
          stdout (.-stdout node/process)]
      (go
        (doseq [file files]
          (try
            (let [fd (<? (call! (.open fs file "r")))
                  byte-chan (file-reader fd)
                  str-chan (string-decoder byte-chan)
                  line-chan (line-transform str-chan)]
              (loop [i 1]
                (when-let [line (<? line-chan)]
                  (when number-lines?
                    (.write stdout (str i " ")))
                  (.write stdout (str line eol))
                  (recur (inc i)))))
            (catch js/Error e
              (handle-error file))))))))

(defn main [& _]
  (node/enable-util-print!)
  (let [opts (parse-args)]
    (dcat opts)))

(set! *main-cli-fn* main)
