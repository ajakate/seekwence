(ns ajakate.seekwence.test-utils
  (:require
   [ajakate.seekwence.core :as core]
   [integrant.repl.state :as state]
   [xtdb.api :as xt]))

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

(def node (atom nil))

(defn xt-node []
  @node)

(defn xtdb-fixture []
  (fn [f]
    (when (not (nil? @node))
      (.close @node))
    (reset! node (xt/start-node {}))
    (f)
    (.close @node)))
