(ns anxietybox.style
  (:require
    [garden.core :as css]))

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
               :font-weight "bold"
               :padding ".25em"
               :color "#777"}]
             [:input.visited
              { :background "#fff"
                :color "#222"
                :font-weight "bold"
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
