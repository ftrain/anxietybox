(ns anxietybox.handler
  (:use compojure.core)
  (:require
    [anxietybox.db :as db]
    [compojure.handler :as handler]
    [compojure.route :as route]
    [garden.core :as css]  
    [hiccup.core :as html]))

(defn check-params [params]
  (filter identity
    (list
      (if (= (:project params) "") "You need to put something in the project box.")
      (if (not (:project params)) "No project.")      
      (if (= (:fullname params) "") "You need to put something in the full name box.")
      (if (not (:fullname params)) "No full name.")            
      (if (= (:email params) "") "You need to put something in the email box.")
      (if (not (:email params)) "No email."))))

(defroutes app-routes
  (GET "/" []
    (html/html [:html
                   [:head
                     [:title "Anxiety Box"]
                     [:style
                       {:type "text/css"}
                       (let [font "Helvetica Neue"]
                         (css/css
                         [:body {:margin 0
                                  :background "#9aa"
                                  :padding "1em"
                                  :font-family font
                                  :font-size "14pt"}]
                         [:h1 {:font-weight "Normal" :line-height "149%"}]
                         [:th {:text-align "right"
                                :color "#44c"
                                :font-size "20pt"
                                :font-style "Italic"
                                :font-weight "Normal"}]
                         [:div.main {:border "1px solid #ccc"
                                      :padding "1em"
                                      :border-radius "1em"
                                      :background "white"}]
                         [:table {:border-collapse "collapse"}]
                         [:th :td {:margin 0 :padding "1em .25em 1em 0"}]
                         [:input {:font-size "20pt"
                                   :width "100%"
                                   :font-family font
                                   :padding ".25em"
                                   :color "#777"}])

                         )]]
                   
                 [:body
                   [:div.main
                     [:h1 "What's in Your Anxiety Box?"]
                     [:div#box
                       [:p "Stop making yourself anxious&mdash;that's our job! When you are anxious <b>your anxiety spams your brain</b> and leads to a scientific condition known as <b>chronic brain explosion thing</b>. But if you leave your anxiety with us we will take over and send you <b>anxious, urgent, deeply upsetting emails</b>. Instead of making yourself miserable we'll do it for you&mdash;and when you delete the email <b>POOF! the anxiety goes away</b>. Happiness is here if you want it."]]
                     [:div#info
                       [:form {:method "post" :action "/"}
                         [:table
                           [:tr
                             [:td [:input {:type "text" :value "your@email.com" :name "email"}]]]
                           [:tr
                             [:td [:input {:type "text" :value "Firstname Lastname" :name "fullname"}]]]
                           [:tr
                             [:td [:input {:type "text" :value "Name your anxiety" :name "project"}]]]]]]
                     ]]]))
  (POST "/" {params :params}
    (let [errors (check-params params)
           id (db/uuid)]
      (if (not= errors ())
        errors
        (do
          (db/box-insert (merge params {:confirm id}))
          (str "<h2>I RECEIVED YOUR ANXIETY!</h2><a href='/confirm/" id "'>CONFIRM</a>" params)))))

  (GET "/confirm/:id" [id]
    (do
      (db/box-activate id)
      (str "IT WORKED")))
  
  (route/resources "/")
  (route/not-found "Not Found"))


  
(def app
  (handler/site app-routes))

(defn bootstrap []
  (do (db/box-insert {:fullname "Paul Ford"
                    :email "ford@ftrain.com"
                    :project "The Secret Lives of Web Pages"
                    :confirm (db/uuid)
                    })))

(bootstrap)
