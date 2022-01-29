(ns trachenberg.language
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [aleph.http :as http]
            [byte-streams :as bs]))

(defn text->entities [text]
  (-> (http/post "https://language.googleapis.com/v1/documents:analyzeEntities"
                 {:query-params {:key ""}
                  :body         (json/generate-string
                                  {:document {:type     "PLAIN_TEXT"
                                              :language "en"
                                              :content  text}})})
      (deref)
      :body
      bs/to-string
      (json/parse-string keyword)
      :entities))


(defn test->sentiment-score [text]
  (-> (http/post "https://language.googleapis.com/v1/documents:analyzeSentiment"
                   {:query-params {:key ""}
                    :body         (json/generate-string
                                    {:document {:type     "PLAIN_TEXT"
                                                :language "en"
                                                :content  text}})})
      (deref)
      :body
      bs/to-string
      (json/parse-string keyword)
      :documentSentiment
      :score))

