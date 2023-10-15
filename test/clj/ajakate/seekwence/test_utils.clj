(ns ajakate.seekwence.test-utils
  (:require
   [ajakate.seekwence.core :as core]
   [integrant.repl.state :as state]
   [clojure.string :as str]
   [clojure.java.io :as io]
   [clojure.data.json :as json]
   [clojure.walk :as w]))

(defn system-state
  []
  (or @core/system state/system))

(defn system-fixture
  []
  (fn [f]
    (when (nil? (system-state))
      (core/start-app {:opts {:profile :test}}))
    (f)
    (core/stop-app)))

(defn covert-string [byte-array-input-stream]
  (let [reader (io/reader byte-array-input-stream)]
    (str/join "" (line-seq reader))))

(defn parse-response-body [input-byte-array]
  (w/keywordize-keys (json/read-str (covert-string input-byte-array))))
