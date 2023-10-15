(ns ajakate.seekwence.game-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [ajakate.seekwence.test-utils :as utils]))

(use-fixtures :each (utils/system-fixture))

(deftest test-game-create
  (testing "GET /api/create it should return a 200"
    (let [response ((:handler/ring (utils/system-state))
                    (-> (mock/request :post "/api/create")
                        (mock/json-body {:name "ajay"})))]
      (is (= 200 (:status response)))
      (is (re-matches
           #"^[A-Z0-9]{4}$"
           (-> response
               :body
               utils/parse-response-body
               :game/id))))))

;; TODO: write test for when the db is full?
;; https://github.com/clojure/data.generators
;; (deftest test-game-error
;;   (testing "GET /api/create it should not hang if bad happens"
;;     (with-redefs [game-controller/room-code-length 2] [game-controller/room-code-chars "ABC"]
;;                  (let [response ((:handler/ring (utils/system-state)) (mock/request :get "/api/create"))]
;;                    (is (= 200 (:status response)))
;;                    (is (re-matches
;;                         #"^[A-Z0-9]{4}$"
;;                         (-> response
;;                             :body
;;                             utils/parse-response-body
;;                             :game/id)))))))
