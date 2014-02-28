(ns anxietybox.handler
  (:use compojure.core)
  (:require
    [anxietybox.data :as data]
    [anxietybox.style :as style] 
    [anxietybox.bot :as bot]    
    [anxietybox.mail :as mail]    
    [compojure.handler :as handler]
    [compojure.route :as route]
    [cheshire.core :as cheshire]    
    [hiccup.core :as html]))

(def site-prefix "http://localhost:3000/")
  
(defn check-params [params]
  (filter identity
          (list
           (if (= (:project params) "") "You need to put something in the project box.")
           (if (not (:project params)) "No project.")      
           (if (= (:fullname params) "") "You need to put something in the full name box.")
           (if (not (:fullname params)) "No full name.")            
           (if (= (:email params) "") "You need to put something in the email box.")
           (if (not (:email params)) "No email."))))



(def js "$(document).ready(function(){ $('input.field').bind('click',
function(s){$(this).attr('value','');}); });")


(defn make-page [title body]
  (html/html
   [:html
    [:head
     [:script {:src "//ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"}]
     [:script js]
     [:link {:href "http://fonts.googleapis.com/css?family=Gentium+Basic:400,400italic,700,700italic|Alfa+Slab+One|Akronim" :rel "stylesheet" :type "text/css"}] 
     [:meta {:type "charset"}]
     [:title title]
     [:style {:type "text/css"} style/css]]
    [:body body
     [:div#footer "An anxiety simulator. &copy;2014 " [:a {:href "http://twitter.com/ftrain"} "Paul Ford."] " All rights reserved. "]
     ]]))

(defroutes app-routes

  (GET "/" []
    (make-page "Anxiety Box"
      (html/html [:div#main
                   [:h1 "What’s in Your" [:br] [:span.sitename "Anxiety Box?"] ]
                   [:h2#quote "&ldquo;" (bot/ps) "&rdquo;" [:nobr [:i "&mdash;" [:a {:href "http://twitter.com/anxietyboxbot"} "@anxietyboxbot" ]]]]

                   [:form {:method "post" :action "/"}

                   [:div#info
                     [:p "Stop making yourself anxious&mdash;that's our job! <b>Fill out the form to the left and leave your anxiety with us.</b>"]

                     [:p "When you're anxious <b>your anxiety spams your mind</b> and leads to a condition known as <b>procrastinatory shame despair</b>."]

                     [:p "We will take over and send you <b>anxious, urgent, deeply upsetting emails</b>. Delete the email and <b>POOF! the anxiety goes away</b>."]

                     [:p "Relief is here if you want it."]]

                     [:div#form
                       [:h3 "your name"]
                       [:input.field {:type "text" :value "First name only" :name "name"}]
                       [:div.gloss "So that we can personalize the terrible emails."]

                       [:h3 "email address"]                       
                       [:input.field {:type "text" :value "your@email.com" :name "email"}]
                       [:div.gloss "Don't worry, every email you get has a link that lets you delete your account."]
                       [:h3 "anxiety name"]
                       [:input.field {:type "text" :value "Finishing my book" :name "project"}]
                       [:div.gloss "What is causing your anxiety? Project X? Turning 40? Losing weight? Stack ranking?"]]
                     [:div.submit-wrapper
                       [:input.submit {:type "submit" :value "Click here to start!"}]]]
                     ])))

  (POST "/" {params :params}
        (let [errors (check-params params)
              id (data/uuid)]
          (if (not= errors ())
            errors
            (let [b (data/box-insert (merge params {:confirm id}))]
              (if (not= (type b) "org.postgresql.util.PSQLException")
                (do
                  (mail/send-confirmation (data/box-insert (merge params {:confirm id})))
                  (make-page "Anxiety Box: Anxiety received"
                             (html/html
                              [:div#main        
                               [:h1 "Anxiety Received"]
                               [:h2 "We received your anxiety. Look for a confirmation email from anxiety@anxietybox.com"]])))
                (do
                  (mail/send-reminder (data/box-insert params))
                  (make-page "Anxiety Box: Duplicate account"
                             (html/html
                              [:div#main                          
                               [:h1 "Duplicate account"]
                               [:h2 "You've already signed up. We sent you an account reminder. It has a link that lets you delete your account and start again."]]))))))))
  
  (GET "/activate/:id" [id]
    (do (let [res (first (data/box-activate id))]
    (make-page "Anxiety Box"
      (html/html [:div#main
                   [:h1 
                     (if (= 1 res) "Activated" "Already activated")]])))))

  (GET "/delete/:id" [id]
    (do (let [res (first (data/box-delete id))]
          (make-page "Anxiety Box: Account deleted"
            (html/html
              [:div#main
                [:h1 "Anxiety Box"]
                [:h2
                (if (= 1 res) "Your account was deleted"
                  "Error: That confirmation has expired.")]])))))

  (GET "/bot" []
    {:headers {"Content-Type" "application/json;charset=UTF-8"}
      :body  (cheshire/generate-string {:statements (take 10 (repeatedly bot/ps))})})

  (GET "/sendmail" []
    {:headers {"Content-Type" "application/json;charset=UTF-8"}
      :body  (cheshire/generate-string (mail/send-emails))})

  (POST "/reply" {params :params}
        {:headers {"Content-Type" "application/json;charset=UTF-8"}
         :body  (cheshire/generate-string (mail/handle-reply params))})

  
  (route/resources "/")
  (route/not-found "Not Found"))


(def app
  (handler/site app-routes))
