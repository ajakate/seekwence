(ns ajakate.seekwence.pages.play
  (:require [ajakate.seekwence.common]
            [ajakate.seekwence.constants :as c]
            [ajakate.seekwence.events]
            [day8.re-frame.http-fx]))

(defn render []
  [:div.grid.grid-cols-10
   (for [row c/board-cards]
     (for [card row]
       [:div.col-span-1.m-1
        [:img {:src (str "/img/cards/" card ".svg") :alt card}]]))])
