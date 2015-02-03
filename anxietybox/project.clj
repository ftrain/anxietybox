(defproject anxietybox "0.2.0-SITE"
  :description "Code for Anxietybox.com"
  :url "https://github.com/ftrain/anxietybox"
  :dependencies [[org.clojure/clojure "1.5.1"]
                  [compojure "1.3.1"]
                  [org.clojure/java.jdbc "0.3.6"]
                  [korma "0.4.0"]
                  [enlive "1.1.5"]
                  [postgresql/postgresql "8.4-702.jdbc4"]
                  [hiccup "1.0.5"]
                  [garden "1.2.5"]
                  [clj-http "1.0.1"]
                  [cheshire "5.4.0"]
                  [com.taoensso/timbre "3.3.1"]
                  [clojurewerkz/quartzite "2.0.0"]
                  [lein-environ "1.0.0"]
                  [de.ubercode.clostache/clostache "1.4.0"]
                  [org.clojure/data.csv "0.1.2"]
                  [environ "1.0.0"]]
  :plugins [[lein-ring "0.9.0"]]
  :ring {:handler anxietybox.handler/app}
  :resource-paths ["src/resources"])
