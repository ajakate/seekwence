(ns ajakate.seekwence.web.services.game
  (:require
   [xtdb.api :as xt]
   [clojure.set :as set]
   [ajakate.seekwence.constants :as c]
   [ajakate.seekwence.web.services.game :as game]))

(def room-code-chars "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")

(def room-code-length 4)

(defn generate-room-code []
  (apply str (repeatedly room-code-length #(str (rand-nth room-code-chars)))))

(defn existing-room-codes [node]
  (xt/q (xt/db node) '{:find [e] :where [[e :xt/id] [e :game/state]]}))

(defn safe-room-code [node]
  (let [existing-room-codes (existing-room-codes node)]
    (loop [code (generate-room-code)]
      (if (not (contains? existing-room-codes [code]))
        code
        (recur (generate-room-code))))))

(defn create! [node name]
  (let [room-code (safe-room-code node)
        player-id (str (random-uuid))]
    (xt/submit-tx
     node
     [[::xt/put
       {:xt/id room-code
        :game/state :init
        :game/players [player-id]}]
      [::xt/put
       {:xt/id player-id
        :player/name name}]])
    (xt/sync node)
    {:game/id room-code
     :player/id player-id
     :player/name name}))

(defn join! [node name code] 
  (let [player-id (str (random-uuid))
        entity (xt/entity (xt/db node) code)
        existing-players (:game/players entity)
        new-entity (assoc entity :game/players (conj existing-players player-id))]
    (xt/submit-tx
     node
     [[::xt/put
       new-entity]
      [::xt/put
       {:xt/id player-id
        :player/name name}]])
    (xt/sync node)
    {:game/id code
     :player/id player-id
     :player/name name}))

(defn format-player-info [node player-id]
  (let [entity (xt/entity (xt/db node) player-id)]
    (set/rename-keys entity {:xt/id :player/id})))

(defn game-code-for-player-id [node player-id]
  (first
   (first
    (xt/q (xt/db node)
          '{:find [game-id]
            :in [player-id]
            :where
            [[g :xt/id game-id]
             [g :game/players player-id]]}
          player-id))))

(defn format-game-info [node game-code]
  (let [game (xt/entity (xt/db node) game-code)
        player-ids (:game/players game)
        players (map #(format-player-info node %) player-ids)]
    (set/rename-keys
     (assoc game :game/players players)
     {:xt/id :game/id})))

(defn get-common-info-by-client-id [node player-id]
  (let [game-code (game-code-for-player-id node player-id)]
     (format-game-info node game-code)))

(defn set-team [node player-id team]
  (let [player (xt/entity (xt/db node) player-id)
        new-entity (assoc player :player/team team)]
    (xt/submit-tx
     node
     [[::xt/put
       new-entity]])
    (xt/sync node)
    (get-common-info-by-client-id node player-id)))

(defn deal-deck [player-ids]
  (let [deck (shuffle (concat c/deck c/deck))
        num-players (count player-ids)
        deal-count (c/cards-to-deal num-players)
        total-cards-to-deal (* deal-count num-players)
        [cards-to-deal remaining-deck] (split-at total-cards-to-deal deck)]
    [(zipmap player-ids (partition deal-count cards-to-deal)) remaining-deck]))

(defn start-game [node game-code]
  (let [game (xt/entity (xt/db node) game-code)
        player-ids (:game/players game)
        [hands deck] (deal-deck player-ids)
        game-txn [[::xt/put
                   (assoc game
                          :game/state :play
                          :game/deck deck
                          :game/turn 0
                          :game/moves [])]]
        player-txn (map
                    (fn [[player-id hand]]
                      (let [entity (xt/entity (xt/db node) player-id)]
                        [::xt/put
                         (assoc entity :player/hand hand)]))
                    hands)
        txn (concat game-txn player-txn)]
    (xt/submit-tx
     node
     txn)
    (xt/sync node)
    (format-game-info node game-code)))

(defn format-for-player [game-info player-id]
  (let [players (map
                 (fn [player]
                   (if (= (:player/id player) player-id)
                     player
                     (dissoc player :player/hand)))
                 (:game/players game-info))
        game (dissoc game-info :game/deck)]
    (assoc game :game/players players)))

(comment 

  (xt/entity (xt/db node) "S850")

  node
  (require '[integrant.repl.state :as state])
  (def node (:db.xtdb/node state/system))

  ;;  
  )
