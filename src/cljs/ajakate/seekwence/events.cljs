(ns ajakate.seekwence.events
  (:require
   [re-frame.core :as rf]
   [reitit.frontend.controllers :as rfc]
   [reitit.frontend.easy :as rfe]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax]
   [akiroz.re-frame.storage :refer [persist-db-keys]]))

(defn persisted-reg-event-db
  [event-id handler]
  (rf/reg-event-fx
   event-id
   [(persist-db-keys :seekwence-app [:games-list])]
   (fn [{:keys [db]} event-vec]
     {:db (handler db event-vec)})))

(persisted-reg-event-db :init-local-storage (fn [db] db))

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
 :set-active-game
 (fn [{:keys [db]} [_ resp]]
   (let [games-list (or (:games-list db) [])] 
     {:fx [[:dispatch [:set-game-db resp]] [:dispatch [:common/redirect :play]]]})))

(persisted-reg-event-db
 :set-game-db
 (fn [db [_ game]]
   (let [games-list (or (:games-list db) [])]
     (assoc db :games-list (conj games-list game)))))

(rf/reg-sub
 :game/id
 (fn [db _]
   (-> db :game/id)))
