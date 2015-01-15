(ns anxietybox.data
  (:require
    [environ.core :as env]
    [korma.db :as korma-db]
    [korma.core :as korma]    
    [taoensso.timbre :as timbre]    
    [clojure.java.jdbc :as sql]))

;; Logging prefix
(timbre/refer-timbre)
(timbre/set-config! [:appenders :spit :enabled?] true)
(timbre/set-config! [:shared-appender-config :spit-filename] (env/env :log-file))
;;

;; Database config
(korma-db/defdb db (korma-db/postgres {:db (env/env :postgres-database)
                                     :user (env/env :postgres-user)
                                     :password (env/env :postgres-password)}))

(korma/defentity accounts
  (korma/entity-fields :created_time :id :name :email :count :confirm :active))
(korma/defentity anxieties)
(korma/defentity replies)

(defn account-insert
  "Create a user account."
  [{name :name email :email :as account}]
  (do
    (log "[account]" account)
    {:result 
      (try
        (korma/insert accounts (korma/values account))
        (catch Exception e
          (do
            (let [e-str (str e)]
              (log "[account-insert error]" e-str
                {:error e-str}))))}))

; (account-insert {:name "paul ford" :email "ford@ftrain.com"})

(defn account-select-by-confirm
  ""
  [confirm]
  (do
    (log "[account-select-by-confirm]" confirm)
    (account-relate
      (first
        (sql/query pg
          ["select * from account where confirm=?" confirm])))))

(defn account-select-by-email
  "Fetch a full record for a account.

   (account-select \"ford@ftrain.com\")

   => {:anxieties [{:description \"finishing my book\", :account_id
   2, :id 1} {:description \"losing weight\", :account_id 2, :id 2}
   {:description \"making friends\", :account_id 2, :id 3}], :active
   true, :confirm #uuid
   \"26ed5e80-ff88-424d-a57b-7e4359ad56bf\", :count 0, :email
   \"ford@ftrain.com\", :name \"Paul\", :id 2, :created_time #inst
   \"2014-03-01T11:55:39.631064000-00:00\"}"
  [email]
  (do
    (log "[account-select-by-email]" email)
    (account-relate (first (sql/query pg ["select * from account where lower(email) = lower(?)" email])))))


  
  (defn anxiety-insert
  "Add an anxiety to the account."
  [{account-id :id :as the-account} {text} anxiety]
  (sql/insert! pg "anxiety"
    {:description (second anxiety) :account_id (:id account)}))


(defn reply-insert
  ""
  [reply]
  (sql/insert! pg "reply" reply))

(defn reply-select
  ""
  [confirm]
  (let [account-id (:id (first (sql/query pg ["select id from account where confirm = ?" confirm])))]
    (sql/query pg ["select * from reply where account_id = ? ORDER BY created_time DESC" account-id])))

(defn reply-select-by-account [account]
  (vec (sql/query pg ["select * from reply where account_id = ? ORDER BY created_time DESC" (:id account)])))

(defn anxiety-select [account]
  (vec (sql/query pg
    ["select * from anxiety where account_id = ?" (:id account)])))

(defn account-relate
  ""
  [account]
  (merge account 
    {:replies (reply-select-by-account account)}
    {:anxieties (anxiety-select account)}))


(defn account-update
  ""
  [account]
  (sql/update! pg "account" (dissoc account :id) ["id=?" (:id account)]))

(defn anxiety-update
  ""
  [anxiety account]
  (sql/insert! pg "anxiety"
    (assoc (dissoc anxiety :id) {:account_id (:id account)})))

(defn toggle-block [code bool]
  ""
  (sql/update! pg "account"
    {:active bool}
    ["confirm=?" code]))

(defn account-activate
  ""
  [code]
  (toggle-block code true))

(defn account-deactivate
  ""
  [code]
  (toggle-block code false))

(defn account-delete
  ""
  [code]
  (sql/delete! pg "account" ["confirm=?" code]))

(defn anxiety-enhance
  ""
  [account]
  (merge account {:anxieties (anxiety-select account)}))

(defn accounts-for-update
  ""
  []
  (map anxiety-enhance (sql/query pg ["SELECT * from account where active=?" true])))


