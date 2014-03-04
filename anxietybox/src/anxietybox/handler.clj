(ns anxietybox.handler
  (:use compojure.core)
  (:require
    [anxietybox.data :as data]
    [anxietybox.style :as style] 
    [anxietybox.bot :as bot]
    [anxietybox.mail :as mail]
    [clojure.string :as string]
    [compojure.handler :as handler]
    [compojure.route :as route]
    [cheshire.core :as cheshire]    
    [hiccup.core :as html]))


(def site-prefix "http://localhost:3000/")
  
(defn check-params [params]
  (filter identity
    (list
           (if (= (:project-0 params) "") "You need to put something in the anxiety box.")
           (if (not (:project-0 params)) "No anxiety entered.")      
           (if (= (:name params) "") "You need to put something in the full name box.")
           (if (not (:name params)) "No full name.")            
           (if (= (:email params) "") "You need to put something in the email box.")
           (if (not (:email params)) "No email."))))


(def js "$(document).ready(function(){ 

$('input.field').bind('focus click', function(s){ 
        $(this).addClass('visited');
        $(this).attr('value','');
}); 

$('#submit').submit(function() {
  $('div#anxieties-wrapper:last-child').delete();
  console.log($(this));
//  return true;
 
});

var no = 0;

anxieties = ['lose weight', 'eat right', 'make friends', 'find love', 'answer email', 'get intimate', 'go to school', 'ask for money', 'break up', 'meet people', 'speak in public', 'be honest', 'tell mom no'];

function getAnxiety(num) {
  if (num > anxieties.length) {return \"that bad, huh?\"}
  else {return anxieties[num - 1];}
}

function form() {
  $('div#anxieties-wrapper').append(
    $('<input class=\"field\" type=\"text\" name=\"project-' + no++ + '\" value=\"' + getAnxiety(no) + '\"/>').bind('focus click',
       function(s) { 
         $(this).addClass('visited');
         $(this).attr('value',''); 
         if ($(this).is(':last-child')) form();}));}
form();
});
")
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

(defn nest [params]
  (reduce (fn [m [ks v]] (assoc-in m ks v)) {}
        (map (fn [[k v]]
               (list (map keyword (string/split (name k) #"-")) v)) params)))

(defroutes app-routes

  (GET "/" []
    (make-page "Anxiety Box"
      (html/html [:div#main
                   [:h1 "What’s in Your" [:br] [:span.sitename "Anxiety Box?"] ]
                   [:h2#quote "&ldquo;" (bot/ps) "&rdquo;" [:nobr [:i "&mdash;" [:a {:href "http://twitter.com/anxietyboxbot"} "@anxietyboxbot" ]]]]

                   [:form {:id "signup" :method "post" :action "/"}

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
                       [:h3 "anxiety stops my efforts to"]

                       [:div#anxieties-wrapper]
                       [:div.gloss "Finishing my book. Managing my eating. Responding to email."]]
                     [:div.submit-wrapper
                       [:input#submit.submit {:type "submit" :value "Click here to start!"}]]]
                     ])))

  (POST "/" {params :params}
    (let [errors (check-params params)
              id (data/uuid)]
      (if (not= errors ())
        
        (make-page "Anxiety Box: Anxiety received"
          (html/html
            [:div#main        
              [:h1 "Looks like you failed, which is typical of you. Please back up and try again."]
              [:h2 errors]]))

        (let [b (data/box-insert
                  (merge params {:confirm id}))]
              (if (not= (type b) "org.postgresql.util.PSQLException")
                (do
                  (mail/send-confirmation (data/box-insert (merge (nest params) {:confirm id})))
                  (make-page "Anxiety Box: Anxiety received"
                             (html/html
                              [:div#main        
                               [:h1 "Anxiety Received"]
                               [:h2 "We received your anxiety, so at least you are able to fill out a form. Look for a confirmation email from anxiety@anxietybox.com"]])))
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

  (GET "/responses" []
    {:headers {"Content-Type" "application/json;charset=UTF-8"}
      :body  (cheshire/generate-string {:statements (take 10 (repeatedly bot/ps))})})

  (GET "/bot" []
    {:headers {"Content-Type" "application/json;charset=UTF-8"}
      :body  (cheshire/generate-string {:statements (take 10 (repeatedly bot/ps))})})

  (GET "/sendmail" []
    {:headers {"Content-Type" "application/json;charset=UTF-8"}
      :body (map mail/send-anxiety (data/boxes-for-update))})
  
  (GET "/receive" {params :params} (cheshire/generate-string {:params params}))

  ; Receives mailgun posts
  (POST "/receive" {params :params} 
        {:headers {"Content-Type" "application/json;charset=UTF-8"}
         :body  (cheshire/generate-string
                 (do (data/reply-insert 
                      {:box_id (:id (data/box-select (:sender params)))
                       :description (:stripped-text params)})))})

  (route/resources "/")
  (route/not-found "Not Found"))


(def app
  (handler/site app-routes))
