(ns ajakate.seekwence.web.controllers.game
  (:require
   [ring.util.http-response :as http-response]
   [xtdb.api :as xt]))

(def room-code-chars "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")

(def room-code-length 4)

(defn generate-room-code []
  (apply str (repeatedly room-code-length #(str (rand-nth room-code-chars)))))

(defn existing-room-codes [node]
  (xt/q (xt/db node) '{:find [e] :where [[e :xt/id]]}))

(defn safe-room-code [node]
  (let [existing-room-codes (existing-room-codes node)]
    (loop [code (generate-room-code)]
      (if (not (contains? existing-room-codes [code]))
        code
        (recur (generate-room-code))))))

(defn create!
  [req]
  (let [node (get-in req [:reitit.core/match :data :db])
        room-code (safe-room-code node)]
    (xt/submit-tx node [[::xt/put
                         {:xt/id room-code}]])
    (http-response/ok
     {:game/id room-code})))
