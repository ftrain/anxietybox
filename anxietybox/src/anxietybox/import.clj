(ns anxietybox.import
  (:require
    [environ.core :as env]
    [anxietybox.data :as data]
    [clojure.data.csv :as csv]
    [clojure.java.io :as io]))

(def the-file "/Users/ford/Dropbox/ab_responses_002.csv")
(defn main- []
  (with-open
    [in-file (io/reader the-file)]
    (doall
      (let [the-records (rest (csv/read-csv in-file))]
        (map (partial apply data/insert-from-csv!) the-records)))))


