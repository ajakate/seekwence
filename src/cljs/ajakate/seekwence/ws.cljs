(ns ajakate.seekwence.ws
  (:require [taoensso.sente :as sente]))

(def ws-connection (atom nil))

;; TODO: can be removed
(defn ch-chsk [] (:ch-recv @ws-connection))

(defn send-message! [& args] (apply (:send-fn @ws-connection) args))

(defn start-connection! [game-token]
  (reset! ws-connection
          (sente/make-channel-socket! "/ws" js/csrfToken {:type :auto :client-id game-token})))

(defn state-handler [{:keys [?data]}]
  (.log js/console (str "state changed: " ?data)))

(defn handshake-handler [{:keys [?data]}]
  (.log js/console (str "connection established: " ?data)))

(defn default-event-handler [ev-msg]
  (.log js/console (str "Unhandled event: " (:event ev-msg))))

(defn event-msg-handler [& [{:keys [message state handshake]
                             :or {state state-handler
                                  handshake handshake-handler}}]]
  (fn [ev-msg]
    (case (:id ev-msg)
      :chsk/handshake (handshake ev-msg)
      :chsk/state (state ev-msg)
      :chsk/recv (message ev-msg)
      (default-event-handler ev-msg))))

(def router (atom nil))

(defn stop-router! []
  (when-let [stop-f @router] (stop-f)))

(defn start-router! [message-handler game-token]
  (stop-router!)
  (start-connection! game-token)
  (reset! router (sente/start-chsk-router!
                   (ch-chsk)
                   (event-msg-handler
                     {:message   message-handler
                      :state     state-handler
                      :handshake handshake-handler}))))
