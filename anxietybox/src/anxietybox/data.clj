(ns anxietybox.data
  (:require
    [clojure.java.jdbc :as sql]))

(defn uuid
  "Generate a UUID.
   (uuid)
   =>#uuid \"f6411771-a11e-40ed-acc8-a844ca2e59cd\"" 
  [] (java.util.UUID/randomUUID))

(def pg {:subprotocol "postgresql"
          :subname "anxietybox"
          :user "postgres"
          :password "waffles"
          :stringtype "unspecified"})

(defn anxiety-insert [box anxiety]
  (sql/insert! pg "anxiety"
    {:description (second anxiety) :box_id (:id box)}))

(defn box-insert [box]
  (try
    (let [db-box (first (sql/insert! pg "box" (dissoc box :project)))]
      (map (partial anxiety-insert db-box) (:project box)))
    (catch Exception e e)))

(defn anxiety-select [box]
  (vec (sql/query pg
    ["select * from anxiety where box_id = ?" (:id box)])))

(defn box-select
  "Fetch a full record for a box.

   (box-select \"ford@ftrain.com\")

   => {:anxieties [{:description \"finishing my book\", :box_id
   2, :id 1} {:description \"losing weight\", :box_id 2, :id 2}
   {:description \"making friends\", :box_id 2, :id 3}], :active
   true, :confirm #uuid
   \"26ed5e80-ff88-424d-a57b-7e4359ad56bf\", :count 0, :email
   \"ford@ftrain.com\", :name \"Paul\", :id 2, :created_time #inst
   \"2014-03-01T11:55:39.631064000-00:00\"}"
  
  [email]
  (let [box (first (sql/query pg ["select * from box where
lower(email) = lower(?)" email]))]
    (merge box {:anxieties (anxiety-select box)})))


(defn box-update [box]
  (sql/update! pg "box" (dissoc box :id) ["id=?" (:id box)]))

(defn anxiety-update [anxiety box]
  (sql/insert! pg "anxiety"
    (assoc (dissoc anxiety :id) {:box_id (:id box)})))

(defn toggle-block [code bool]
  (sql/update! pg "box"
    {:active bool :confirm (uuid)}
    ["confirm=?" code]))

(defn box-activate
  [code]
  (toggle-block code true))

(defn box-deactivate
  [code]
  (toggle-block code false))

(defn box-delete
  [code]
  (sql/delete! pg "box" ["confirm=?" code]))

(defn boxes-for-update []
  (sql/query pg ["SELECT * from box where active=?" true]))


