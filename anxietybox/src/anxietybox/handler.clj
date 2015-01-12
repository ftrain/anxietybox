(ns anxietybox.handler
  (:use compojure.core)
  (:require
    [taoensso.timbre :as timbre]    
    [anxietybox.data :as data]
    [anxietybox.style :as style] 
    [anxietybox.bot :as bot]
    [anxietybox.mail :as mail]
    [clojure.string :as string]
    [compojure.handler :as handler]
    [compojure.route :as route]
    [cheshire.core :as cheshire]    
    [hiccup.core :as html]))

;; Logging prefix
(timbre/refer-timbre)
(timbre/set-config! [:appenders :spit :enabled?] true)
;(timbre/set-config! [:shared-appender-config :spit-filename] (env/env :log-file))

(def site-prefix "http://localhost:3000/")

(defn nest
  "Take a key-value map and nest it by splitting on dashes.

   (nest {:type \"Student\" :name-first \"John\" :name-last
   \"Doe\" :class-1 \"English\" :class-2 \"Biology\" :class-3
   \"Gym\"})

   => {:type \"Student\", :class {:3 \"Gym\", :2 \"Biology\", :1
   \"English\"}, :name {:first \"John\", :last \"Doe\"}}"
  [params]
  (reduce (fn [m [ks v]] (assoc-in m ks v)) {}
        (map (fn [[k v]]
               (list (map keyword (string/split (name k) #"-")) v)) params)))

(defn check-params
  "A dumb parameter checker."
  [params]
  (filter identity
    (list
           (if (= (:project-0 params) "") "You need to put something in the anxiety box.")
           (if (not (:project-0 params)) "No anxiety entered.")      
           (if (= (:name params) "") "You need to put something in the full name box.")
           (if (not (:name params)) "No full name.")            
           (if (= (:email params) "") "You need to put something in the email box.")
           (if (not (:email params)) "No email."))))


(defn make-js
  "TK"
  [& params]
(str "$(document).ready(function(){ 

var values=
"
  (if (ffirst params) (cheshire/generate-string ((comp first first first) params) {:pretty true}) {})
  
  ";

$('input.field').bind('focus click', function(s){ 
        $(this).addClass('visited');
        $(this).attr('value','');
}); 

$('#submit').submit(function() {
  $('div#anxieties-wrapper:last-child').delete();
  return true;
});

var no = 0;

var anxieties = ['lose weight', 'eat right', 'make friends', 'find love', 'answer email', 'get intimate', 'go to school', 'ask for money', 'break up', 'meet people', 'speak in public', 'be honest', 'tell mom no'];

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
"))

(defn make-page
  "Make an HTML page"
  [title body & params]
  (html/html
   [:html
    [:head
      [:script {:src "//ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"}]
      [:style {:type "text/css"} style/css]      
      [:script (make-js params)]
     [:link {:href "http://fonts.googleapis.com/css?family=Gentium+Basic:400,400italic,700,700italic|Alfa+Slab+One|Akronim" :rel "stylesheet" :type "text/css"}] 
     [:meta {:type "charset"}]
     [:title title]
      [:body body
        [:div#footer "An anxiety simulator. &copy;2014 "
          [:a {:href "http://twitter.com/ftrain"} "Paul Ford."]
          " All rights reserved. "]]]]))

;; TK make this templates
(defn make-home [& params]
  (make-page "Anxiety Box"
    (html/html [:div#main
                 [:h1 "What’s in Your" [:br] [:span.sitename "Anxiety Box?"] ]
                 [:h2#quote "&ldquo;"
                   (bot/ps)
                   "&rdquo;"
                   [:nobr
                     [:i "&mdash;"
                       [:a
                         {:href "http://twitter.com/anxietyboxbot"}
                         "@anxietyboxbot" ]]]]
                 [:form
                   {:id "signup" :method "post" :action "/"}
                   [:div#info
                     [:p "Stop making yourself anxious&mdash;that's
                     our job! <b>Fill out the form to the left and
                     leave your anxiety with us.</b>"]
                     
                     [:p "When you're anxious <b>your anxiety spams
                     your mind</b> and leads to a condition known as
                     <b>procrastinatory shame despair</b>."]
                     
                     [:p "We will take over and three or four times
                     a day send you <b>anxious, urgent, deeply
                     upsetting emails</b>. Delete the email (or
                     reply) and <b>POOF! the anxiety goes
                     away</b>. Delete your account any time."]

                     [:p "Relief is here if you want it."]]

                     [:div#form
                       [:h3 "my name is"]
                       [:input.field {:type "text"
                                       :value "First name only"
                                       :name "name"}]
                       [:div.gloss "So that we can personalize the terrible emails."]

                       [:h3 "my email is"]                       
                       [:input.field {:type "text"
                                       :value "your@email.com"
                                       :name "email"}]
                       [:div.gloss "Don't worry, every email you get has a link that lets you delete your account."]
                       [:h3 "my anxiety won't let me"]
                       [:div#anxieties-wrapper]
                       [:div.gloss "Finish my book. Manage my eating. Respond to email. Enter as many as you want."]]
                     [:div.submit-wrapper
                       [:input#submit.submit {:type "submit" :value "Click here to start!"}]]]])
    params))


(defroutes app-routes

  (GET "/" [] (make-home))

  (POST "/" {params :params}
    (let [errors (check-params params)
           id (data/uuid)]
      (info params errors id)
      (if (not= errors ())
        
        (make-page "Anxiety Box: Errors"
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

  (GET "/edit/:confirm" [confirm]
    (make-home (data/box-select-by-confirm confirm)))
  
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


  ; Receives mailgun posts
  (POST "/receive" {params :params} 
        {:headers {"Content-Type" "application/json;charset=UTF-8"}
         :body  (cheshire/generate-string
                 (do (data/reply-insert 
                      {:box_id 
                       (:id  (let [box (data/box-select (:sender params))]
                               (if box box 
                                   (let [conf
                                         (drop 1 (re-find #"/delete/([\S\"]+)" (:body-plain params)))]
                                     (if conf (data/box-select-by-confirm (first conf)))))))
                       :description (:stripped-text params)})))})

  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))

;(mail/send-anxiety (data/box-select "ford@ftrain.com"))
;(map mail/send-anxiety (data/boxes-for-update))
