(ns trachenberg.alpha-vantage
  (:require [cheshire.core :as json]
            [aleph.http :as http]
            [byte-streams :as bs]
            [cuerdas.core :as str]))


(defn api-request [opts]
  (-> (http/get "https://www.alphavantage.co/query"
                {:query-params opts})
      (deref)
      :body
      (bs/to-string)
      (json/parse-string str/kebab)))


(defn intraday [{:keys [symbol interval output-size datatype api-key]}]
  (api-request {:function    "TIME_SERIES_INTRADAY"
                :symbol      (name symbol)
                :interval    interval
                :output-size (or output-size "compact")
                :datatype    (or datatype "json")
                :apikey     (or api-key "KXBF0O72IMTLQ7XB")}))
