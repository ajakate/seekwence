(ns ajakate.seekwence.events
  (:require
   [re-frame.core :as rf]
   [reitit.frontend.controllers :as rfc]
   [reitit.frontend.easy :as rfe]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax]
   [ajakate.seekwence.ws :as ws]
   [akiroz.re-frame.storage :refer [persist-db-keys]]))

;; TODO: move
(defn ws-handler [resp]
  (println "response: " resp))

(defn persisted-reg-event-db
  [event-id handler]
  (rf/reg-event-fx
   event-id
   [(persist-db-keys :seekwence-app [:games-list])]
   (fn [{:keys [db]} event-vec]
     {:db (handler db event-vec)})))

(persisted-reg-event-db :init-local-storage (fn [db] db))

(rf/reg-fx
 :start-websocket
 (fn [token]
   (ws/start-router! ws-handler token)))

(rf/reg-fx
 :stop-websocket
 (fn [_]
   (ws/stop-router!)))

(rf/reg-event-fx
 :create-game
 (fn [_ [_ name]]
   {:http-xhrio {:method           :post
                 :uri              "/api/create"
                 :params           {:name name}
                 :format           (ajax/json-request-format)
                 :response-format  (ajax/json-response-format {:keywords? true})
                 :on-success       [:set-active-game]}}))

(rf/reg-event-fx
 :join-game
 (fn [_ [_ name code]]
   {:http-xhrio {:method           :post
                 :uri              "/api/join"
                 :params           {:name name :code code}
                 :format           (ajax/json-request-format)
                 :response-format  (ajax/json-response-format {:keywords? true})
                 :on-success       [:set-active-game]}}))

(rf/reg-event-fx
 :set-active-game
 (fn [_ [_ resp]]
   {:fx [[:dispatch [:set-game-db resp]] [:dispatch [:common/redirect :play {:game-code (:game/id resp)}]]]}))

(persisted-reg-event-db
 :set-game-db
 (fn [db [_ game]]
   (let [games-list (or (:games-list db) [])]
     (assoc db :games-list (conj games-list game)))))

(rf/reg-event-fx
 :play-controller
 (fn [{:keys [db]} [_ room-code]]
   (let [user-token (->> (:games-list db)
                         (filter #(= (:game/id %) room-code))
                         (map :player/id)
                         first)]
     {:start-websocket user-token})))

(rf/reg-event-fx
 :home-controller
 (fn [_ [_ _]]
   {:stop-websocket nil}))

(rf/reg-sub
 :game/id
 (fn [db _]
   (-> db :game/id)))
