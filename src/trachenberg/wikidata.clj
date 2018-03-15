(ns trachenberg.wikidata
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [aleph.http :as http]
            [byte-streams :as bs]
            [trachenberg.language :as lang]))


(defn mid-to-ticker-query [company-mid]
  (format
    "SELECT ?companyLabel ?rootLabel ?tickerLabel ?exchangeNameLabel
     WHERE {?entity wdt:P646 \"%s\" .
     ?entity wdt:P176* ?manufacturer .
     ?manufacturer wdt:P156* ?company .
     { ?company p:P414 ?exchange } UNION
     { ?company wdt:P127+ / wdt:P156* ?root .
     ?root p:P414 ?exchange } UNION
     { ?company wdt:P749+ / wdt:P156* ?root .
     ?root p:P414 ?exchange } .
     VALUES ?exchanges { wd:Q13677 wd:Q82059 } .
     ?exchange ps:P414 ?exchanges .
     ?exchange pq:P249 ?ticker .
     ?exchange ps:P414 ?exchangeName .
     FILTER NOT EXISTS { ?company wdt:P31 /
     wdt:P279* wd:Q1616075 } .
     FILTER NOT EXISTS { ?company wdt:P31 /
     wdt:P279* wd:Q11032 } .
     SERVICE wikibase:label { bd:serviceParam wikibase:language \"en\" . } }
     GROUP BY ?companyLabel ?rootLabel ?tickerLabel ?exchangeNameLabel
     ORDER BY ?companyLabel ?rootLabel ?tickerLabel ?exchangeNameLabel"
    company-mid))


(lang/text->entities "Apple said will build a new plant in Baja, Mexico, to build Corolla cars for U.S. NO WAY! Build plant in U.S. or pay big border tax.")
;
;(mid-to-ticker-query "/m/07mb6")
(defn mid->company-ticker [mid]
  (let [{{company :value}  :companyLabel
         {ticker :value}   :tickerLabel
         {exchange :value} :exchangeNameLabel}
        (-> (http/get "https://query.wikidata.org/sparql"
                        {:query-params  {:query (mid-to-ticker-query mid)}
                         :accept        :json
                         :client-params {:cookie-policy (constantly nil)}})
            (deref)
            :body
            bs/to-string
            (json/parse-string keyword)
            (get-in [:results :bindings])
            (first))]
    (when ticker
      {:company  company
       :ticker   ticker
       :exchange exchange})))


(defn filter-organizations [xs]
  (->> xs
       (filter #(#{"ORGANIZATION"} (:type %)))))


(defn entity->company-ticker [entity]
  (mid->company-ticker (get-in entity [:metadata :mid])))


(defn entities->company-tickers [entities]
  (->> entities
       (map #(entity->company-ticker %))
       (filter identity)
       (distinct)))


