(ns anxietybox.mail
  (:require
    [environ.core :as env]    
    [taoensso.timbre :as timbre]    
    [anxietybox.bot :as bot]
    [clojurewerkz.quartzite.scheduler :as qs]
    [clojurewerkz.quartzite.triggers :as t]    
    [clj-http.client :as client]))

(timbre/refer-timbre)
(timbre/set-config! [:appenders :spit :enabled?] true)
;; TODO put this in env.
(timbre/set-config! [:shared-appender-config :spit-filename] "/Users/ford/Desktop/logs/clojure.log")

(def mail-api-auth ["api" (env/env :mail-api-api-key)])
(def mail-api-site "anxietybox.com")
(def mail-api-uri (str "https://api.mail-api.net/v2/" mail-api-site "/messages"))
(def from-email (env/env :from-email) )

(defn mail-api-send 
  "Post an email to the mail-api gateway."
  [form]
  (info form)
  (client/post mail-api-uri
    {:basic-auth mail-api-auth
      :throw-entire-message? true
      :form-params (merge {:from from-email} form)}))

(defn send-confirmation
  ""
  [box]
  (info box)
  (mail-api-send { :to (:email box)
          :subject "Confirmation requested"
          :text (str "Dear " (:name box) ",
\nYou just signed up for Anxietybox.com. Click here to confirm your email:
\n\thttp://anxietybox.com/activate/" (:confirm box) "
\nIf you didn't sign up, ignore this email." closing)}))

(defn anxiety-text
  [box]
  (str "Dear " (:name box) ",\n\n"
    (bot/compose 
      (if (:anxieties box) (:description (rand-nth (:anxieties box))))
      (if (:reply box) (:description (rand-nth (:replies box)))))
    closing
    "\n\nP.S. Click here to delete your account:"
    "\n\thttp://anxietybox.com/delete/"
    (:confirm box) 
    "\nYou can start a new account any time."))

(defn send-anxiety
  [box]
  (mail-api-send { :to (:email box)
          :subject (bot/ps)
          :text (anxiety-text box)}))

(defn -main
  [& m]
  (let [s   (-> (qs/initialize) qs/start)
       job  (j/build
              (j/of-type NoOpJob)
              (j/with-identity (j/key "jobs.noop.1")))
        trigger (t/build
                  (t/with-identity (t/key "triggers.1"))
                  (t/start-now)
                  (t/with-schedule (schedule
                                     (with-interval-in-minutes 2)
                                     (monday-through-friday)
                                     (starting-daily-at (time-of-day 9 00 00))
                                     (ending-daily-at (time-of-day 17 00 00)))))]
  (qs/schedule s job trigger)))


