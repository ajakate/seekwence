(ns ajakate.seekwence.core
  (:require
   [ajakate.seekwence.common]
   [ajakate.seekwence.events]
   [reagent.core :as r]
   [re-frame.core :as rf]
   [day8.re-frame.http-fx]
   [reitit.frontend.easy :as rfe]
   [reitit.core :as reitit]
   [ajakate.seekwence.pages.play :as play-page]
   [reagent.dom :as d]))

;; -------------------------
;; Views

(defn page []
  (when-let [page @(rf/subscribe [:common/page])]
    [:div.flex.justify-center>div.max-w-5xl
     [page]]))

(defn home-page []
  (let [draft_name_create (r/atom nil)
        draft_name_join (r/atom nil)
        draft_code_join (r/atom nil)]
    (fn []
      [:div.flex.flex-col.justify-center
       [:p.m-5.font-bold.text-xl "Welcome to Seekwence!"]
       [:input
        {:type "text"
         :placeholder "sk8hkr69"
         :on-change #(reset! draft_name_create (.. % -target -value))
         :value @draft_name_create}]
       [:button.m-5
        {:on-click #(rf/dispatch [:create-game @draft_name_create])}
        "Create a New Game"]
       
       [:input
        {:type "text"
         :placeholder "your name"
         :on-change #(reset! draft_name_join (.. % -target -value))
         :value @draft_name_join}]
       [:input
        {:type "text"
         :placeholder "game code"
         :on-change #(reset! draft_code_join (.. % -target -value))
         :value @draft_code_join}]
       [:button.m-5
        {:on-click #(rf/dispatch [:join-game @draft_name_join @draft_code_join])}
        "Join Existing Game"]
       [:h1 "Existing Games"]
       [:table
        (let [games-list @(rf/subscribe [:games-list])]
          [:tbody
           (for [game games-list]
             ^{:key (:game/id game)}
             [:tr (:game/id game) " " (:player/name game) " " 
              [:button.m-5
               {:on-click #(rf/dispatch [:delete-game (:game/id game)])}
               "Delete"]])])]])))

(defn play-page []
  (let [game-code @(rf/subscribe [:game/id])
        players  @(rf/subscribe [:game/players])
        game-state @(rf/subscribe [:game/state])]
    [:div.flex.flex-col
     [:h1.m-1 (str "Welcome to Game " game-code)]
     (case game-state
       :init
       [:div
        [:table
         [:tbody
          (for [player players]
            ^{:key (:player/id player)}
            [:tr (:player/name player) " " (:player/team player)])]]
        [:button.m-5
         {:on-click #(rf/dispatch [:set-teams])}
         "set teams"]
        [:button.m-5
         {:on-click #(rf/dispatch [:start-game])}
         "start game"]]
       [play-page/render])]))

;; -------------------------
;; Initialize app

(defn navigate! [new-match]
  (when new-match
    (rf/dispatch [:common/navigate new-match])))

(def router
  (reitit/router
   [["/" {:name        :home
          :view        #'home-page
          :controllers [{:start (fn [_] (rf/dispatch [:home-controller]))}]}]
    ["/play/:game-code"
     {:name :play
      :view   #'play-page
      :controllers [{:parameters {:path [:game-code]}
                     :start (fn [{{:keys [game-code]} :path}]
                              (rf/dispatch [:play-controller game-code]))}]}]]))

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
  (rf/dispatch-sync [:init-local-storage])
  (mount-root))
