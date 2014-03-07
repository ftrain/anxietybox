(ns anxietybox.data
  (:require
    [environ.core :as env]
    [taoensso.timbre :as timbre]    
    [clojure.java.jdbc :as sql]))

;; Logging prefix
(timbre/refer-timbre)
(timbre/set-config! [:appenders :spit :enabled?] true)
(timbre/set-config! [:shared-appender-config :spit-filename] (env/env :log-file))
;;

(defn uuid
  "Generate a UUID.
   (uuid)
   =>#uuid \"f6411771-a11e-40ed-acc8-a844ca2e59cd\"" 
  [] (java.util.UUID/randomUUID))

(def pg {:subprotocol "postgresql"
          :subname "anxietybox"
          :user (env/env :postgres-user)
          :password (env/env :postgres-password)
          :stringtype "unspecified"})

(defn anxiety-insert
  ""
  [box anxiety]
  (sql/insert! pg "anxiety"
    {:description (second anxiety) :box_id (:id box)}))

(defn box-insert
  [box]
  (log "[box]" box)
  (try
    (let [db-box (first (sql/insert! pg "box" (dissoc box :project)))]
      (doall (map (partial anxiety-insert db-box) (:project box)))
      db-box)
    (catch Exception e e)))

(defn reply-insert [reply]
  (sql/insert! pg "reply" reply))

(defn reply-select [confirm]
  (let [box-id (:id (first (sql/query pg ["select id from box where confirm = ?" confirm])))]
    (sql/query pg ["select * from reply where box_id = ? ORDER BY created_time DESC" box-id])))

(defn reply-select-by-box [box]
  (vec (sql/query pg ["select * from reply where box_id = ? ORDER BY created_time DESC" (:id box)])))

(defn anxiety-select [box]
  (vec (sql/query pg
    ["select * from anxiety where box_id = ?" (:id box)])))

(defn box-relate [box]
  (merge box 
    {:replies (reply-select-by-box box)}
    {:anxieties (anxiety-select box)}))

(defn box-select-by-confirm
  [confirm]
  (prn confirm)
  (box-relate
    (first
      (sql/query pg
        ["select * from box where confirm=?" confirm]))))

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
  (box-relate (first (sql/query pg ["select * from box where lower(email) = lower(?)" email]))))



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

(defn anxiety-enhance [box]
  (merge box {:anxieties (anxiety-select box)}))

(defn boxes-for-update []
  (map anxiety-enhance (sql/query pg ["SELECT * from box where active=?" true])))

;(boxes-for-update)
