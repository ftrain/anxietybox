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
          :user "unscroll"
          :password "waffles"
          :stringtype "unspecified"})

(defn box-insert [box]
  (try
    (sql/insert! pg "box" box)
    (catch Exception e e)))

(defn box-select [email]
  (first (sql/query pg
           ["select * from box where lower(email) = lower(?)" email])))

(defn box-update [box]
  (sql/update! pg "box" (dissoc box :id) ["id=?" (:id box)]))

(defn anxiety-insert [anxiety box]
  (sql/insert! pg "anxiety"
    (assoc anxiety {:box_id (:id box)})))

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

; (defn get-boxes
  

; (box-select "ford@localhost")

