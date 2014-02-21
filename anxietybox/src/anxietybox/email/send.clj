(ns anxietybox.email.send
  (:require
    [anxietybox.db :as db]
    [clojurewerkz.mailer.core :as mail]))

;; set default delivery mode (:smtp, :sendmail or :test)
(mail/delivery-mode! :sendmail)
(def default-email "Your Anxiety <your.anxiety@anxietybox.com>")

(defn send-confirmation [box]
  (mail/deliver-email {:from default-email
                        :to [(:email box)]
                        :subject (:subject "[AnxietyBox.com] Confirmation requested")}
    "templates/activate.mustache"
    box
  :text/html))

(defn send-reminder [box]
  (mail/deliver-email {:from default-email
                        :to [(:email box)]
                        :subject (:subject "[AnxietyBox.com] Account info")}
    (if (:active box)"templates/deactivate.mustache" "templates/activate.mustache")
    box
  :text/html))

    
(defn send-anxiety [box anxiety]
  (mail/deliver-email {:from default-email
                        :to [(:email box)]
                        :subject (:subject anxiety)}
    "templates/anxiety.mustache"
    (merge box anxiety)
  :text/html))


