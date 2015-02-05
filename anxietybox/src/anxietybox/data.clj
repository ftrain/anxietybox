(ns anxietybox.data
  (:require
    [environ.core :as env]
    [korma.db :as korma-db]
    [korma.core :as korma]    
    [taoensso.timbre :as timbre]    
    [clojure.java.jdbc :as sql]))

(import '(org.postgresql.util PGobject))
;; Logging prefix
(timbre/refer-timbre)
(timbre/set-config! [:appenders :spit :enabled?] true)
(timbre/set-config! [:shared-appender-config :spit-filename] (env/env :log-file))
(timbre/set-level! :debug)

(defn str->pgobject
  [type value]
  (doto (PGobject.)
    (.setType type)
    (.setValue value)))

;; Database config
(korma-db/defdb db
  (korma-db/postgres {:db (env/env :postgres-database)
                       :user (env/env :postgres-user)
                       :password (env/env :postgres-password)}))

;; The database is three tables: account, anxiety, and reply.
;; We don't use passwords or anything else. This is controlled
;; purely via email.

(declare account anxiety reply mail_to_send)
(korma/defentity account
  (korma/entity-fields [:id :account_id] :name :email [:tracker :account_tracker] :active)
  (korma/has-many anxiety)  
  (korma/prepare
    (fn [{tracker :tracker :as v}]
      (assoc v :tracker (str->pgobject "uuid" tracker)))))

;; Tracker is a UUID that we can use to track
;; emails. People never give you an honest email and they reply
;; using multiple emails. So when we send an anxiety we'll
;; include a unique ID that we can put into the email prefixed by
;; anxious: or some similar identifier; we can parse that out via
;; regex and use it to look up the anxiety--and then we'll know the
;; account, and we'll be able to store the reply.

(korma/defentity anxiety
  (korma/entity-fields :created_time [:id :anxiety_id] :account_id :tracker :description)
  (korma/belongs-to account)
  (korma/has-many reply))

(korma/defentity reply
  (korma/entity-fields :created_time [:id :reply_id] :account_id :anxiety_id :body)
  (korma/belongs-to anxiety))

(korma/defentity mail_to_send
  (korma/entity-fields [:id :mail_id] :anxiety_id :send_time :sent)
  (korma/belongs-to anxiety))

(defn account-insert
  "Create a user account."
  [the-account]
  (if-let [db-account (first
                        (korma/select account
                          (korma/where (select-keys the-account [:email]))))]
    (merge db-account {:existed true})
    (do
      (log :info "[data/account-insert]" the-account)
      (try
        (korma/insert account (korma/values the-account))
        (catch Exception e
          (log :debug "[data/account-insert error]" (str e) the-account))))))

(defn account-activate
  "Given a UUID set the account to active."
  [tracker]
  (korma/update account
    (korma/set-fields {:active true})    
    (korma/where {:tracker (str->pgobject "uuid" tracker)})))

(defn anxiety-insert
  "Make an anxiety (as a string) and associate it with an account."
  [the-account the-anxiety]
  (let [data {:account_id (:id the-account) :description the-anxiety}]
    (if-let [db-anxiety (first (korma/select anxiety (korma/where data)))]
      (merge db-anxiety {:existed true})
      (try
        (korma/insert anxiety (korma/values data))
        (catch Exception e
          (log :debug "[data/anxiety-insert error]" (str e) data))))))

(defn insert-from-csv!
  ""
  [timestamp name email anxiety_one anxiety_two description]
  (let [the-account (account-insert {:name name :email email})]
    (map (partial anxiety-insert the-account) [anxiety_one anxiety_two])))

(defn anxiety-insert-many
  ""
  [the-account the-anxieties]
  (map (partial anxiety-insert the-account) the-anxieties))

(defn anxiety-delete
  ""
  [the-account the-anxiety]
  (korma/delete anxiety (korma/where (select-keys the-anxiety [:id]))))

(defn anxiety-update
  ""
  [the-account the-anxiety new-anxiety]
  (korma/update anxiety
    (korma/where (select-keys the-anxiety [:id]))
    (korma/values {:description new-anxiety})))

(defn get-account-by
  ""
  [where-map]
  (first
    (korma/select account
      (korma/with anxiety)
      (korma/where where-map))))

;Every 8 hours we want to send a user 1 emails.
;get all the users 
;if we haven't sent an email by then we should.


;select from account, count (log)
;where needs_anxiety > 0;

(defn make-fake-account
  ""
  []
  (let [no (int (* 10000000 (rand)))]
    (hash-map
      :name (str "Paul " no)
      :email (str "ford+" no "@ftrain.com"))))

(def fake-anxieties ["My colon will explode."
               "I won't be able to lose weight."
               "I'll die alone."
               "I'm a bad father."])

(defn make-many-fake-accounts
  []
  (map (fn [fake-acct]
         (let [the-account (account-insert fake-acct)]
           (anxiety-insert-many the-account fake-anxieties)))
    (take 100 (repeatedly make-fake-account))))

(defn fetch-emails-to-send
  []
  (korma/select mail_to_send
    (korma/with anxiety
      (korma/with account))
    (korma/where {:sent false
                   :send_time [<= (korma/sqlfn now)]} )))

(defn fetch-accounts-to-intro
  []
  (korma/select account
    (korma/where {:active false})))

(defn send-opt-in-email
  [acct]
  (prn
    (str "Sending opt-in email to " (:email acct))))

(defn send-scheduled-emails
  []
  (map (fn [acct]
         (do
           ; Set sent to true
           (korma/update mail_to_send
             (korma/set-fields {:sent true})    
             (korma/where {:id (:mail_id acct)}))
           ; Send email
           (prn
             (str "Fake-sending email to " (:email acct)))))
    (fetch-emails-to-send)))
