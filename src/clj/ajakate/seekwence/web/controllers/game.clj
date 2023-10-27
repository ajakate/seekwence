(ns ajakate.seekwence.web.controllers.game
  (:require
   [ring.util.http-response :as http-response] 
   [ajakate.seekwence.web.services.game :as game]))

(defn create!
  [{{:keys [name]} :body-params :as request}]
  (let [node (get-in request [:reitit.core/match :data :db])]
    (http-response/ok
     (game/create! node name))))

(defn join!
  [{{:keys [name code]} :body-params :as request}]
  (let [node (get-in request [:reitit.core/match :data :db])] 
    (http-response/ok
     (game/join! node name code))))
