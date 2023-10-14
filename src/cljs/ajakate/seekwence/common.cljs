(ns ajakate.seekwence.common
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [day8.re-frame.http-fx] 
   [reitit.frontend.controllers :as rfc]))

(rf/reg-event-db
 :init-db
 (fn [db _]
   (if db
     db
     {:common/route nil})))

(rf/reg-sub
 :common/route
 (fn [db _]
   (-> db :common/route)))

(rf/reg-sub
 :common/page
 :<- [:common/route]
 (fn [route _]
   (-> route :data :view)))

(rf/reg-event-db
 :common/navigate
 (fn [db [_ new-match]]
   (let [old-match   (:common/route db)
         controllers (rfc/apply-controllers (:controllers old-match) new-match)]
     (assoc db :common/route (assoc new-match :controllers controllers)))))
