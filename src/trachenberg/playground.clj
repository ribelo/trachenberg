(ns trachenberg.playground
  (:require [cheshire.core :as json]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [taoensso.encore :as e]
            [cuerdas.core :as str]
            [clj-time.core :as dt]
            [clj-time.format :as dtf]
            [clj-time.coerce :as dtc]
            [net.cgrand.xforms :as x]))

(def data (json/parse-string (slurp "trump.json") str/keyword))

(def trades
  (->> data
       (map (fn [{:keys [text stocks created-at] :as m}]
              (for [{:keys [sentiment] :as stock} stocks]
                (when (not= 0 sentiment)
                  (assoc stock :created-at (dtc/from-string created-at)
                               :text text)))))
       (flatten)
       (filter identity)
       (sort-by :created-at)))

(def trade (first trades))
(count trades)

(defn get-stock-data [ticker]
  (with-open [reader (io/reader (str "resources/stock_data/" (str/lower ticker) "_us_d.csv"))]
    (let [csv-data (->> (doall (csv/read-csv reader))
                        (rest)
                        (map zipmap
                             (->> [:date :open :high :low :close :volume]
                                  (map keyword)
                                  repeat)))]
      (map (fn [m]
             (-> m
                 (update :open str/parse-double)
                 (update :high str/parse-double)
                 (update :low str/parse-double)
                 (update :close str/parse-double)
                 (update :volume str/parse-double)
                 (update :date #(dtf/parse (dtf/formatter "YYYY-MM-dd") %))))
           csv-data))))


(defn get-percentage-change [data]
  (let [o (:open (first data))
        c (:close (last data))]
    (- (/ c o) 1.0)))


(defn get-trade-profit
  ([{:keys [ticker created-at] :as trade} candles]
   (let [created-at' (dt/date-time (dt/year created-at)
                                   (dt/month created-at)
                                   (dt/day created-at))
         stock-data (->> (get-stock-data ticker)
                         (partition-by (fn [{:keys [date]}]
                                         (= date created-at')))
                         (last)
                         (take candles))]
     (println (:company trade) created-at' (:date (first stock-data)))
     (get-percentage-change stock-data)))
  ([trade]
   (get-trade-profit trade 1)))

(defn sign [x]
  (cond (pos? x) 1
        (neg? x) -1
        :else 0))

(def trades-with-change
  (->> trades
       (map (fn [{:keys [sentiment] :as trade}]
              (let [pct (get-trade-profit trade)]
                (assoc trade :percent-change pct
                             :profit (if (= (sign sentiment) (sign pct)) pct (- pct))))))
       (doall)))

(->> trades-with-change
     ;(sort-by :profit)
     (map :profit)
     (reduce +)
     ;(filter #(neg? %))
     ;(count)
     )
(:date (first (get-stock-data "F")))

(get-trade-profit (nth trades 15))
trades-with-change