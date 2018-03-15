(ns trachenberg.trump
  (:require [clojure.java.io :as io]
            [cheshire.core :as json]
            [cuerdas.core :as str]
            [clj-time.format :as dtf]))


(def data
  (-> "trump_data/2017.json"
      (io/resource)
      (slurp)
      (json/parse-string str/keyword)
      (->> (map (fn [m] (update m :created-at #(dtf/parse (dtf/formatter "EEE MMM dd HH:mm:ss Z YYYY") %)))))))

(first data)