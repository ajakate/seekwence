(ns ajakate.seekwence.env
  (:require
    [clojure.tools.logging :as log]
    [ajakate.seekwence.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[seekwence starting using the development or test profile]=-"))
   :start      (fn []
                 (log/info "\n-=[seekwence started successfully using the development or test profile]=-"))
   :stop       (fn []
                 (log/info "\n-=[seekwence has shut down successfully]=-"))
   :middleware wrap-dev
   :opts       {:profile       :dev
                :persist-data? true}})
