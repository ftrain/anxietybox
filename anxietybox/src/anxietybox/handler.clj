(ns anxietybox.handler
  (:use compojure.core)
  (:require
    [anxietybox.sql :as sql]
    [anxietybox.bot :as bot]    
    [anxietybox.email.send :as send]    
    [compojure.handler :as handler]
    [compojure.route :as route]
    [cheshire.core :as cheshire]    
    [garden.core :as css]  
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

(def css
  (let [font "'Gentium Basic', serif"
         title-font "'Alfa Slab One', cursive"
         display-font "'Akronim', cursive"]
    (css/css {:pretty-print? false }
      [:body
        {:margin-left "auto"
          :margin-right "auto"
          :background "red"
          :padding "1em"
          :max-width "800px"

          :font-family font
          :font-size "12pt"
          :text-align "center"}]
      [:a {:color "inherit"}]
      [:h1 :h2
        {:font-family title-font
          :font-weight "normal"
          :margin 0
          :padding 0
          :color "black"
          :text-transform "uppercase"
          :font-size "220%"
          :line-height "175%"
          }]
      [:span.sitename
        {:font-family display-font
          :font-size "300%"
          :z-index "-1"
          :text-transform "uppercase"
          :color "red"
          }]
      [:b {:color "red"}]
      [:h2
        {:font-size "130%"
          :font-family font
          :font-weight "bold"
          :margin "1em 5em 1em 5em"
          :padding 0
          :text-transform "none"
          :line-height "129%"
          :color "#888"
          }]
      [:h3 {:margin 0
             :padding 0
             :font-variant "Italic"
             :font-weight "normal"
             :color "#88A"
             }]
      [:div#main
        {
          :padding "1em"
          :border-radius "1em"
          :border-left "20px solid #C00"
          :border-top "20px solid #D00"
          :border-right "20px solid #B00"
          :border-bottom "20px solid #A00"
          :box-shadow "4px 4px 10px 0px rgba(0, 0, 0, 0.9)"
          :background "white"}]
      [:div#info
        { :width "50%"
          :padding "-1em 0 0 0"
          :text-align "left"
          :font-size "13pt"
          :float "right"
          
          }]
      [:div#form
        {:color "#666"
          :padding "0 1em 0 0"
          :margin "0 1em 0 0"
          :width "45%"
          :text-align "right"
          :border-right "1px dotted #ccc"
          :font-style "italic"
          }]
      [:div.gloss
        {:padding ".5em 0 1em 0"
          :line-height "129%"
          :font-size "90%"
          }]
      [:input.field
        { :font-size "12pt"
          :background "#ffe"
          :text-align "right"
          :clear "both"
          :font-family font
          :font-weight 500
          :padding ".25em"
          :color "#777"}]
      [:input.field:hover :input.field:focus
        { :background "#fff"
          :color "#333"
          }]
      [:div#footer {:margin-top "20px" :text-align "center" :color "pink"}]
      [:div.submit-wrapper
        {:width "100%"
          :padding-top "1em"
          :text-align "center"}
        ]
      [:input.submit
        {:margin-left "auto"
          :margin-right "auto"
          :display "block"
          :font-size "25pt"
          :border "3px solid red"
          :font-family title-font
          :background "red"
          :color "white"
          :border-radius "1em"
          :border-left "4px solid #C00"
          :border-top "4px solid #D00"
          :border-right "4px solid #B00"
          :border-bottom "4px solid #A00"          
          :text-transform "uppercase"
          :padding "0 .5em 0 .5em"}]

      [:input.submit:hover :input.submit:focus
        {:background "#F66"}]
      [:input.submit:active
        {:background "#F66"
          :border-right "4px solid #C00"
          :border-bottom "4px solid #D00"
          :border-left "4px solid #B00"
          :border-top "4px solid #A00"          
          }]      

  
      )))


(def js "$(document).ready(function(){
$('input.field').bind('click', function(s){$(this).attr('value','');});
});")
(defn make-page [title body]
  (html/html
    [:html
      [:head
        [:script {:src "//ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"}]
        [:script js]
        [:link {:href "http://fonts.googleapis.com/css?family=Gentium+Basic:400,400italic,700,700italic|Alfa+Slab+One|Akronim" :rel "stylesheet" :type "text/css"}] 
        [:meta {:type "charset"}]
        [:title title]
      [:style {:type "text/css"} css]]
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
                       [:input.field {:type "text" :value "First name only" :name "fullname"}]
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
           id (sql/uuid)]
      (if (not= errors ())
        errors
        (let [b (sql/box-insert (merge params {:confirm id}))]
          (if (not= (type b) "org.postgresql.util.PSQLException")
            (do
              (send/send-confirmation (sql/box-insert (merge params {:confirm id})))
              (make-page "Anxiety Box: Anxiety received"
                (html/html
                  [:div#main        
                    [:h1 "Anxiety Received"]
                    [:h2 "We received your anxiety. Look for a confirmation email from anxiety@anxietybox.com"]])))
            (do
              (send/send-reminder (sql/box-insert params))
              (make-page "Anxiety Box: Duplicate account"
                (html/html
                  [:div#main                          
                    [:h1 "Duplicate account"]
                    [:h2 "You've already signed up. We sent you an account reminder. It has a link that lets you delete your account and start again."]]))))))))

  (GET "/bot" []
    {:headers {"Content-Type" "application/json;charset=UTF-8"}
      :body  (cheshire/generate-string {:statements (take 10 (repeatedly bot/ps))})})

  
  (GET "/activate/:id" [id]
    (do (let [res (first (sql/box-activate id))]
    (make-page "Anxiety Box"
      (html/html [:div#main
                   [:h1 
                     (if (= 1 res) "Activated" "Already activated")]])))))

  (GET "/delete/:id" [id]
    (do (let [res (first (sql/box-delete id))]
          (make-page "Anxiety Box: Account deleted"
            (html/html
              [:div#main
                [:h1 "Anxiety Box"]
                [:h2
                (if (= 1 res) "Your account was deleted"
                  "Error: That confirmation has expired.")]])))))

  
  (route/resources "/")
  (route/not-found "Not Found"))


(def app
  (handler/site app-routes))
