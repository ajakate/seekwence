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

(comment

  (run-tests)

  (run-all-tests)

  ;;
  )
