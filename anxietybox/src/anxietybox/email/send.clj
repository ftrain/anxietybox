(ns anxietybox.email.send
  (:use compojure.core)
  (:require
    [anxietybox.db :as db]
    [clojurewerkz.mailer.core :as mail]))

;; set default delivery mode (:smtp, :sendmail or :test)
(mail/delivery-mode! :sendmail)

(defn send-confirmation [box]

  )

    
(defn send-anxiety [box anxiety]
  (mail/deliver-email {:from "Your Anxiety <your.anxiety@anxietybox.com>"
                        :to [(:email box)]
                        :subject (:subject anxiety)}
    "templates/anxiety.mustache"
    (merge box anxiety)
  :text/html))

(send-email
  (db/box-select "ford@ftrain.com")
  {:subject "You are a terrible failure."})

