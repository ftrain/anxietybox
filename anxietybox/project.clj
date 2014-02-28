(defproject anxietybox "0.1.0-SNAPSHOT"
  :description "Code for Anxietybox.com"
  :url "http://anxietybox.com/source"
  :dependencies [[org.clojure/clojure "1.5.1"]
                  [compojure "1.1.6"]
                  [org.clojure/java.jdbc "0.3.3"]
                  [enlive "1.1.5"]
                  [postgresql/postgresql "8.4-702.jdbc4"]
                  [hiccup "1.0.5"]
                  [garden "1.1.5"]                  
                  [clojurewerkz/mailer "1.0.0"]
                  [cheshire "5.3.1"]                  
                  [com.taoensso/timbre "3.0.1"]
                  [http-kit "2.1.16"]
                                        ; project.clj
                  ]
  :plugins [[lein-ring "0.8.10"]]
  :ring {:handler anxietybox.handler/app}
  :resource-paths ["src/resources"]
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
