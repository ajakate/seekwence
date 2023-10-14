(ns ajakate.seekwence.core
  (:require
   [ajakate.seekwence.common]
   [reagent.core :as r]
   [re-frame.core :as rf]
   [day8.re-frame.http-fx]
   [reitit.frontend.easy :as rfe] 
   [reitit.core :as reitit]
   [reagent.dom :as d]))

;; -------------------------
;; Views

(defn page []
  (when-let [page @(rf/subscribe [:common/page])] [page]))

(defn home-page []
  [:div.flex.flex-col.justify-center
   [:h1.m-1 "Welcome to Seekwence!"]
   [:button "Create a New Game"]
   [:button "Join Existing Game"]])

(defn new-page []
  [:div.flex.flex-col.justify-center
   [:h1.m-1 "play page! test"]
   [:button "Create a New Game"]
   [:button "Join Existing Game"]])

;; -------------------------
;; Initialize app

(defn navigate! [new-match]
  (when new-match
    (rf/dispatch [:common/navigate new-match])))

(def router
  (reitit/router
   [["/" {:name        :home
          :view        #'home-page
          :controllers [{:start (fn [_] ())}]}]
    ["/play" {:name     :new
              :view   #'new-page
              :controllers [{:start (fn [_] ())}]}]]))

(defn start-router! []
  (rfe/start!
   router
   navigate!
   {}))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (d/render [#'page] (.getElementById js/document "app")))

(defn ^:export ^:dev/once init! []
  (start-router!)
  (rf/dispatch-sync [:init-db])
  (mount-root))
