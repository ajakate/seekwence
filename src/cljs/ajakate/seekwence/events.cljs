(ns ajakate.seekwence.events
  (:require
   [re-frame.core :as rf]
   [reitit.frontend.controllers :as rfc]
   [reitit.frontend.easy :as rfe]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax]
   [ajakate.seekwence.ws :as ws]
   [akiroz.re-frame.storage :refer [persist-db-keys]]))

(enable-console-print!)

(defn ws-handler [resp]
  (let [[id response] (second (:event resp))]
    (rf/dispatch [:handle-websocket-event id response])
    (println "response: " resp)))

(rf/reg-event-fx
 :handle-websocket-event
 (fn [_ [_ id response]]
   (let [handler (keyword (str "ws-handler/" (name id)))]
     {:fx [[:dispatch [handler response]]]})))

(rf/reg-event-fx
 :ws-handler/game-info
 (fn [{:keys [db]} [_ response]]
   {:db (merge db response)}))

(rf/reg-event-fx
 :ws-handler/set-team
 (fn [{:keys [db]} [_ response]]
   {:db (merge db response)}))

(rf/reg-event-fx
 :ws-handler/start-game
 (fn [{:keys [db]} [_ response]]
   {:db (merge db response)}))

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
 :websocket
 (fn [[path & message]]
   (ws/send-message! [path message])))

(rf/reg-event-fx
 :get-game-info
 (fn [_ [_ _]]
   {:websocket [:seekwence/game-info]}))

(rf/reg-event-fx
 :start-game
 (fn [_ [_ _]]
   {:websocket [:seekwence/start-game]}))

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
   {:stop-websocket nil
    :fx [[:dispatch :sync-existing-games]]}))

(rf/reg-event-fx
 :sync-existing-games
 (fn [_ [_ _]]))

(rf/reg-event-fx
 :set-teams
 (fn [{:keys [db]} [_ _]]
   (let [player-ids (map :player/id (:game/players db))]
     {:fx [[:dispatch [:set-team (first player-ids) "Red"]] [:dispatch [:set-team (second player-ids) "Green"]]]})))

(rf/reg-event-fx
 :set-team
 (fn [_ [_ player-id color]]
   {:websocket [:seekwence/set-team {:player/id player-id :player/team color}]}))

(persisted-reg-event-db
 :delete-game
 (fn [db [_ game-id]]
   (assoc db :games-list (filter #(not= (:game/id %) game-id) (:games-list db)))))

(rf/reg-sub
 :game/id
 (fn [db _]
   (-> db :game/id)))

(rf/reg-sub
 :game/players
 (fn [db _]
   (-> db :game/players)))

(rf/reg-sub
 :games-list
 (fn [db _]
   (-> db :games-list)))
