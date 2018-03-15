(ns trachenberg.core
  (:require [clojure.core.async :refer [go go-loop]]
            [trachenberg.trump :as trump]
            [trachenberg.wikidata :as wiki]
            [trachenberg.language :as lang]
            [cheshire.core :as json]
            [cheshire.generate :as json-generate]
            [clj-time.coerce :as dtc]))


(defn add-stocks-sentiment [{:keys [text] :as twitt}]
  (let [sentiment (lang/test->sentiment-score text)
        entities (lang/text->entities text)
        stocks (->> entities
                    (map #(wiki/entity->company-ticker %))
                    (filter identity)
                    (distinct)
                    (map #(assoc % :sentiment sentiment))
                    (not-empty))]
    (assoc twitt :stocks stocks)))

(def db (atom []))
(def rest-data (atom nil))
(def stop? (atom false))

(go-loop [[twitt & twitts] (or @rest-data trump/data)
          i 0]
  (if (and twitt (not @stop?))
    (do
      (println (count @rest-data))
      (swap! db conj (add-stocks-sentiment twitt))
      (reset! rest-data (vec twitts))
      (recur twitts (inc i)))
    (println "stop!")))

(reset! stop? false)

(json-generate/add-encoder
  org.joda.time.DateTime
  (fn [data jsonGenerator]
    (.writeString jsonGenerator (dtc/to-string data))))

(json/generate-string (first @db))

(spit "trump.json" (json/generate-string @db {:pretty true}))
