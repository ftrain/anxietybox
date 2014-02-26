(ns anxietybox.email.send
  (:require
    [anxietybox.bot :as bot]    
    [anxietybox.sql :as sql]    
    [clojurewerkz.mailer.core :as mail]))


;; set default delivery mode (:smtp, :sendmail or :test)

; (mail/delivery-mode! :sendmail)

(def default-email "Your Anxiety <anxiety@anxietybox.com>")

(defn prefix [s] (str "[anxietybox.com]" s))

(defn send-confirmation [box]
  (mail/deliver-with-sendmail {:from default-email
                        :to [(:email box)]
                        :subject (prefix "Confirmation requested")}
    "templates/activate.mustache"
    box
  :text/html))

(sql/box-select "ford@ftrain.com")
(defn send-reminder [box]
  (mail/deliver-with-sendmail {:from default-email
                        :to [(:email box)]
                        :subject (prefix "Account info")}
    (if (:active box)"templates/deactivate.mustache" "templates/activate.mustache")
    box
  :text/html))

(defn send-anxiety [box anxiety]
  (if (:active box)
    (mail/deliver-with-sendmail {:from default-email
                                 :to [(:email box)]
                                 :subject (:subject anxiety)}
                                "templates/anxiety.mustache"
                                (merge box anxiety)
                                :text/html)))


(defn send-emails [] {:response "OKAY"})

(defn handle-reply [reply] {:response "OKAY" :reply reply})




