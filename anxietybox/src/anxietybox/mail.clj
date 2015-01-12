(ns anxietybox.mail
  (:require
    [environ.core :as env]    
    [taoensso.timbre :as timbre]    
    [anxietybox.bot :as bot]    
    [clj-http.client :as client]))

(timbre/refer-timbre)
(timbre/set-config! [:appenders :spit :enabled?] true)
;; TODO put this in env.
(timbre/set-config! [:shared-appender-config :spit-filename] "/Users/ford/Desktop/logs/clojure.log")

(def mailgun-auth ["api" (env/env :mailgun-api-key)])
(def mailgun-site "anxietybox.com")
(def mailgun-uri (str "https://api.mailgun.net/v2/" mailgun-site "/messages"))
(def from-email "Your Anxiety <anxiety@anxietybox.com>")
(def closing "\n\nSincerely,\n\nYour Anxiety\n\nhttp://anxietybox.com // http://twitter.com/anxietyboxbot")

(defn mailgun-send 
  "Post an email to the mailgun gateway."
  [form]
  (info form)
  (client/post mailgun-uri
    {:basic-auth mailgun-auth
      :throw-entire-message? true
      :form-params (merge {:from from-email} form)}))

(defn send-confirmation
  ""
  [box]
  (info box)
  (mailgun-send { :to (:email box)
          :subject "Confirmation requested"
          :text (str "Dear " (:name box) ",
\nYou just signed up for Anxietybox.com. Click here to confirm your email:
\n\thttp://anxietybox.com/activate/" (:confirm box) "
\nIf you didn't sign up, ignore this email." closing)}))

(defn send-reminder [box]
  (mailgun-send { :to (:email box)
          :subject "Account information"
          :text (str "Dear " (:name box) ",
\nClick here to delete your account:
\n\thttp://anxietybox.com/delete/" (:confirm box) "
\nYou can start a new account any time." closing)}))

(defn anxiety-text [box]
  (str "Dear " (:name box) ",\n\n"
    (bot/compose 
      (if (:anxieties box) (:description (rand-nth (:anxieties box))))
      (if (:reply box) (:description (rand-nth (:replies box)))))
    closing
    "\n\nP.S. Click here to delete your account:"
    "\n\thttp://anxietybox.com/delete/"
    (:confirm box) 
    "\nYou can start a new account any time."
    ))

(defn send-anxiety [box]
  (mailgun-send { :to (:email box)
          :subject (bot/ps)
          :text (anxiety-text box)}))


;(send-anxiety (data/box-select "ford@ftrain.com"))



