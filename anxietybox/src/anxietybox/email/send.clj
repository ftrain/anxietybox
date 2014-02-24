(ns anxietybox.email.send
  (:require
    [anxietybox.bot :as bot]    
    [clojurewerkz.mailer.core :as mail]))

;; set default delivery mode (:smtp, :sendmail or :test)

(mail/delivery-mode! :sendmail)

(def default-email "Anxiety <anxiety@anxietybox.com>")

(defn prefix [s] (str "[anxietybox.com]" s))

(defn send-confirmation [box]
  (mail/deliver-email {:from default-email
                        :to [(:email box)]
                        :subject (prefix "Confirmation requested")}
    "templates/activate.mustache"
    box
  :text/html))

(defn send-reminder [box]
  (mail/deliver-email {:from default-email
                        :to [(:email box)]
                        :subject (prefix "Account info")}
    (if (:active box)"templates/deactivate.mustache" "templates/activate.mustache")
    box
  :text/html))

(send-reminder
  (db/box-select "ford@localhost"))

(defn send-anxiety [box anxiety]
  (mail/deliver-email {:from default-email
                        :to [(:email box)]
                        :subject (:subject anxiety)}
    "templates/anxiety.mustache"
    (merge box anxiety)
  :text/html))

    


