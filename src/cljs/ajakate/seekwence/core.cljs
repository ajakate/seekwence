(ns ajakate.seekwence.core
  (:require
   [ajakate.seekwence.common]
   [ajakate.seekwence.events]
   [reagent.core :as r]
   [re-frame.core :as rf]
   [day8.re-frame.http-fx]
   [reitit.frontend.easy :as rfe]
   [reitit.core :as reitit]
   [ajakate.seekwence.ws :as ws]
   [reagent.dom :as d]))

;; -------------------------
;; Views

(defn page []
  (when-let [page @(rf/subscribe [:common/page])]
    [:div.flex.justify-center>div.max-w-5xl
     [page]]))

(defn home-page []
  (let [draft_name (r/atom nil)]
    (fn []
      [:div.flex.flex-col.justify-center
       [:p.m-5.font-bold.text-xl "Welcome to Seekwence!"]
       [:input
        {:type "text"
         :placeholder "sk8hkr69"
         :on-change #(reset! draft_name (.. % -target -value))
         :value @draft_name}]
       [:button.m-5
        {:on-click #(rf/dispatch [:create-game @draft_name])}
        "Create a New Game"]
       [:button.m-5
        {:on-click #(ws/send-message! [:guestbook/echo "Hallo Server"])}
        "Join Existing Game"]])))

(defn play-page []
  (let [game-code @(rf/subscribe [:game/id])]
    [:div.flex.flex-co
     [:h1.m-1 (str "Welcome to Game " game-code)]
     [:button.m-5
      {:on-click #(ws/send-message! [:guestbook/broadcast "Hallo Server"])}
      "click me"]]))

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
    ["/play" {:name     :play
              :view   #'play-page
              :controllers [{:start (fn [_] ())}]}]]))

(defn start-router! []
  (rfe/start!
   router
   navigate!
   {}))

(defn ws-handler [resp]
  (println "response: " resp))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (d/render [#'page] (.getElementById js/document "app")))

(defn ^:export ^:dev/once init! []
  (ws/start-router! ws-handler)
  (start-router!)
  (rf/dispatch-sync [:init-db])
  (mount-root))
