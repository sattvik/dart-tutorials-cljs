(ns dart-tutorials.server
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :refer [redirect]]))

(defroutes routes
  (GET "/" [] (redirect "/index.html"))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (handler/site routes))
