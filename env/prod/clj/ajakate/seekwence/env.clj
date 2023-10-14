(ns ajakate.seekwence.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[seekwence starting]=-"))
   :start      (fn []
                 (log/info "\n-=[seekwence started successfully]=-"))
   :stop       (fn []
                 (log/info "\n-=[seekwence has shut down successfully]=-"))
   :middleware (fn [handler _] handler)
   :opts       {:profile :prod}})
