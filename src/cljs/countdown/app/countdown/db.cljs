(ns countdown.db
  (:require [cljs.core.async :as async :refer [>! <! chan close! put!]]
            [goog.db :as db]
            [goog.db.Transaction :as Transaction]
            [goog.db.Cursor :as Cursor]
            [goog.events :as events]
            [countdown.messages :refer [make-error make-value error?]]
            [countdown.milestone :as ms])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(def db-name "milestoneStore")
(def db-version 1)
(def name-index "name_index")

(defonce db-instance (atom nil))

(defn- read-cursor
  [{:keys [cursor] :as db}]
  (let [c (chan)
        result (chan)]
    (events/listen cursor
                   #js [Cursor/EventType.COMPLETE Cursor/EventType.NEW_DATA
                        Cursor/EventType.ERROR]
                   #(put! c %))
    (.log js/console "cursor:" cursor)
    (go
      (loop [db db]
    (.log js/console "txn:" (:transaction db))
        (when-let [event (<! c)]
          (condp = (.-type event)
            Cursor/EventType.COMPLETE
              (>! result (make-value :done))
            Cursor/EventType.NEW_DATA
              (do
                (.next cursor)
                (recur db))
            Cursor/EventType.ERROR
              (>! result (make-error event))
            (>! result (make-error "Unexpected event type while reading cursor." event))))
        (close! c)
        (close! result)))
    result))

(defn open []
  #_(db/deleteDatabase db-name)
  (let [c (chan)]
    (letfn [(on-upgrade [_ db transaction]
              (if-let [store (.createObjectStore db db-name #js {:autoIncrement true})]
                (when-not (.createIndex store name-index "milestoneName" #js {:unique true})
                  (.abort transaction))
                (.abort transaction)))
            (on-success [db _]
              (swap! db-instance #(do
                                    (when %
                                      (.close %))
                                    db))
              (let [trans (.createTransaction db
                                              #js [db-name]
                                              Transaction/TransactionMode.READ_ONLY)
                    store (.objectStore trans db-name)
                    getter (.getAll store)]
                (doto getter
                  (.addCallback #(go
                                   (>! c (make-value (mapv ms/raw->Milestone %)))
                                   (close! c)))
                  (.addErrback #(go
                                  (>! c (make-error %))
                                  (close! c))))))
            (on-error [err]
              (go
                (>! c (make-error err))
                (close! c)))]
      (doto (db/openDatabase db-name db-version on-upgrade)
        (.addCallback on-success)
        (.addErrback on-error)))
    c))

(defn close []
  (swap! db-instance #(when %
                        (.close %)
                        nil)))

(defn key-for-name
  [name]
  (let [c (chan)]
    (if-let [db @db-instance]
      (let [txn (.createTransaction db
                                    #js [db-name]
                                    Transaction/TransactionMode.READ_ONLY)
            store (.objectStore txn db-name)
            index (.getIndex store name-index)
            key   (atom nil)]
        (events/listen txn
                       Transaction/EventTypes.COMPLETE
                       #(go
                          (.log js/console "key-for-name: complete" %)
                          (if @key
                            (>! c (make-value @key))
                            (>! c (make-error "No key was found, somehow.")))
                          (close! c)))
        (events/listen txn
                         Transaction/EventTypes.ERROR
                         #(go
                            (.log js/console "key-for-name: error:" %)
                            (>! c (make-error %))
                            (close! c)))
        (-> index
            (.getKey name)
            (.addCallback (fn [v] (reset! key v)))))
      (go
        (>! c (make-error "No database instance."))
        (close! c)))
    c))

(defn add-milestone
  [{:keys [milestoneName] :as milestone}]
  (let [c (chan)]
     (if-let [db @db-instance]
        (let [txn (.createTransaction db
                                      #js [db-name]
                                      Transaction/TransactionMode.READ_WRITE)
              store (.objectStore txn db-name)]
          (events/listen txn
                         Transaction/EventTypes.COMPLETE
                         #(go
                            (>! c (make-value milestone))
                            (close! c)))
          (events/listen txn
                         Transaction/EventTypes.ERROR
                         #(go
                            (>! c (make-error (.-target %)))
                            (close! c)))
          (.add store (ms/Milestone->raw milestone)))
        (go
          (>! c (make-error "No database instance."))
          (close! c)))
    c))

(defn remove-milestone
  [milestone]
  (let [c (chan)]
    (if-let [db @db-instance]
      (go
        (let [key (<! (key-for-name (.-milestoneName milestone)))]
          (if (error? key)
            (do
              (>! c (make-error key))
              (close! c))
            (let [txn (.createTransaction db
                                          #js [db-name]
                                          Transaction/TransactionMode.READ_WRITE)
                  store (.objectStore txn db-name)]
              (events/listen txn
                             Transaction/EventTypes.COMPLETE
                             #(go
                                (>! c (make-value milestone))
                                (close! c)))
              (events/listen txn
                             Transaction/EventTypes.ERROR
                             #(go
                                (>! c (make-error (.-target %)))
                                (close! c)))
              (.remove store (:value key))))))
      (go
        (>! c (make-error "No database instance."))
        (close! c)))
    c))

(defn clear-milestones
  []
  (let [c (chan)]
    (go
      (if-let [db @db-instance]
        (let [txn (.createTransaction db
                                      #js [db-name]
                                      Transaction/TransactionMode.READ_WRITE)
              store (.objectStore txn db-name)]
          (events/listen txn
                         Transaction/EventTypes.COMPLETE
                         #(go
                            (>! c (make-value []))
                            (close! c)))
          (events/listen txn
                         Transaction/EventTypes.ERROR
                         #(go
                            (>! c (make-error (.-target %)))
                            (close! c)))
          (.clear store))
        (go
          (>! c (make-error "No database instance."))
          (close! c))))
    c))
