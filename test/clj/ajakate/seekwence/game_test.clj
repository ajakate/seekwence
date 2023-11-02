(ns ajakate.seekwence.game-test
  (:require [clojure.test :refer :all]
            [ajakate.seekwence.test-utils :as utils]
            [ajakate.seekwence.web.services.game :as game]
            [xtdb.api :as xt]))

(use-fixtures :each (utils/xtdb-fixture))

(deftest test-create
  (testing "it should create a game"
    (let [node (utils/xt-node)
          resp (game/create! node "ajay")]
      (is (re-matches
           #"^[A-Z0-9]{4}$"
           (:game/id resp)))
      (is (= "ajay" (:player/name resp))))))

(deftest test-join
  (testing "it should let two people join"
    (let [node (utils/xt-node)
          resp1 (game/create! node "ajay")
          game-id (:game/id resp1)
          resp2 (game/join! node "liz" game-id)
          entity (xt/entity (xt/db node) game-id)]
      (is (= (:game/players entity) [(:player/id resp1) (:player/id resp2)]))
      (is (= (:game/state entity) :init)))))

(deftest test-get-common-info-by-player-id
  (testing "it should find a game by player id"
    (let [node (utils/xt-node)
          game-resp (game/create! node "ajay")
          player-id (:player/id game-resp)
          game-id  (:game/id game-resp)
          game-resp2 (game/join! node "liz" game-id)]
      (is (= (game/get-common-info-by-client-id node player-id)
             {:game/state :init
              :game/id game-id
              :game/players
              [{:player/name "ajay"
                :player/id (:player/id game-resp)}
               {:player/name "liz"
                :player/id (:player/id game-resp2)}]})))))

(deftest test-team-join
  (testing "it should assign a team to a player"
    (let [node (utils/xt-node)
          game-resp (game/create! node "ajay")
          player-id (:player/id game-resp)
          resp (game/set-team node player-id "Blue")]
      (is (= (-> resp :game/players first :player/team) "Blue")))))

(deftest start-game
  (testing "it should start the game and deal out the deck properly"
    (let [node (utils/xt-node)
          resp1 (game/create! node "ajay")
          ajay-id (:player/id resp1)
          game-id (:game/id resp1)
          resp2 (game/join! node "liz" game-id)
          liz-id (:player/id resp2)
          _ (game/start-game node game-id)
          game-entity (xt/entity (xt/db node) game-id)
          ajay-entity (xt/entity (xt/db node) ajay-id)
          liz-entity (xt/entity (xt/db node) liz-id)
          deck-length (count (:game/deck game-entity))
          ajay-length (count (:player/hand ajay-entity))
          liz-length (count (:player/hand liz-entity))]
      (is (= (:game/state game-entity) :play))
      (is (= (:game/turn game-entity) 0))
      (is (= ajay-length 7))
      (is (= liz-length 7))
      (is (= deck-length 90)))))

(deftest format-for-player
  (testing "it should hide the deck and other player hand info"
    (let [node (utils/xt-node)
          resp1 (game/create! node "ajay")
          ajay-id (:player/id resp1)
          game-id (:game/id resp1)
          resp2 (game/join! node "liz" game-id)
          liz-id (:player/id resp2)
          resp (game/start-game node game-id)
          ajay-resp (game/format-for-player resp ajay-id)
          liz-resp (game/format-for-player resp liz-id)]
      (println ajay-resp)
      (is (= (:game/deck ajay-resp) nil))
      (is (= (:game/deck liz-resp) nil))
      (is (= (count (:player/hand (first (:game/players ajay-resp)))) 7))
      (is (= (:player/hand (second (:game/players ajay-resp))) nil))
      (is (= (count (:player/hand (second (:game/players liz-resp)))) 7))
      (is (= (:player/hand (first (:game/players liz-resp))) nil)))))

(comment

  (run-tests)

  (run-all-tests)

  ;;
  )
