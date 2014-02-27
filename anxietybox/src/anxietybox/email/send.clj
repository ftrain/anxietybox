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
  :text/plain))


(defn send-reminder [box]
  (mail/deliver-with-sendmail {:from default-email
                        :to [(:email box)]
                        :subject (prefix "Account info")}
    (if (:active box)"templates/deactivate.mustache" "templates/activate.mustache")
    box
  :text/plain))

(defn send-anxiety [box]
  (if (:active box)
    (mail/deliver-with-sendmail {:from default-email
                                 :to [(:email box)]
                                 :subject "More on your failings"}
                                "templates/anxiety.mustache"
                                (merge box {:anxiety (bot/ps)})
                                :text/plain)))



(defn send-emails [] (send-anxiety (sql/box-select "ford@ftrain.com")))

(defn handle-reply [reply] (prn {:response "OKAY" :reply reply}))

