(ns ajakate.seekwence.events
  (:require
   [re-frame.core :as rf]
   [reitit.frontend.controllers :as rfc]
   [reitit.frontend.easy :as rfe]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax]))

(rf/reg-event-fx
 :create-game
 (fn []
   {:http-xhrio {:method          :get
                 :uri             "/api/create"
                 :response-format  (ajax/json-response-format {:keywords? true})
                 :on-success       [:set-active-game]}}))

(rf/reg-event-fx
 :set-active-game
 (fn [{:keys [db]} [_ resp]]
   {:db (assoc db :resp (:game/id resp))
    :fx [[:dispatch [:redirect-to-play]]]}))

(rf/reg-event-fx
 :redirect-to-play
 (fn [_ [_]]
   (rfe/push-state :play)))
