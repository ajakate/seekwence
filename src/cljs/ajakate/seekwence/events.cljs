(ns ajakate.seekwence.events
  (:require
   [re-frame.core :as rf]
   [reitit.frontend.controllers :as rfc]
   [reitit.frontend.easy :as rfe]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax]))

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
   {:db (merge db resp)
    :fx [[:dispatch [:redirect-to-play]]]}))

(rf/reg-event-fx
 :redirect-to-play
 (fn [_ [_]]
   (rfe/push-state :play)))

(rf/reg-sub
 :game/id
 (fn [db _]
   (-> db :game/id)))
