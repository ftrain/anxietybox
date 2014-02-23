(ns anxietybox.bot
  (:require
    [clojure.string :as string]))

(def indicators ["all signs point to you being"
                  "You:"
                  "Your issues in a nutshell: you're"
                  "just relax and stop pretending to be anything other than"
                  "your friends accept you even though you are undeniably"
                  "it's unfair that so many people say you're"
                  "you don't really deserve to be seen as"
                  "no one wants to tell you that you are"
                  "i wish you could accept that you are"
                  "all of your friends were trying to describe you and what came up was:"
                  "people are only tired of you because you are so"
                  "all the people you meet immediately think:"
                  "a tag cloud of your personality would include things like"
                  "i wonder how can you succeed at anything given that you are so"
                  "When I look at you, I think:"
                  "no one can blame you for being born"
                  "you mom and dad would never say anything but they so want to know why you choose to be"
                  "it's clear that you don't know what to do besides be"
                  "i respect that you just live your life and don't care if people think you are"
                  "most of your friends are doing okay, which makes me wonder why are you so"
                  "the only explanation for your career is that you are essentially"
                  "when you meet successful people they think of you as" 
                  "Your total lack of self-control has left you"
                  "people pretend to be nice to you but they're thinking:"
                  "when you aren't around your friends say you are"
                  "it's true that you are basically"
                  "history will forget you because history forgets people who are"
                  "unlike successful people you are totally"
                  "compared to you, people who succeed are not so"
                  "unlike you, your 'friends' are not"
                  "the reason no one notices your work is because you are"
                  "the reason you are not famous is because you are"
                  "obviously you would go further in life except for your tendency to be"                  
                  "you could be successful if you were not"
                  "you might succeed if you didn't seem so"
                  "your parents deserve credit for loving someone so"
                  "compared to the rest of your family you sure are"
                  "compared with everyone else you grew up with you definitely turned out"
                  "Your whole life you've just been"
                  "your friends all say you are"
                  "like most people who procrastinate, you're"
                  "i don't agree with all of the people who say that you are"
                  "your mother always worried that you would turn out"
                  "if your grandparents were here right now they'd tell you that you are"
                  "people on Facebook look at your picture and thinks:"
                  "the minute you got in the cab your cab driver thought: I'm"
                  "it's a shame that people have painted you as"
                  "it's too bad everyone has decided you are"
                  "it's not entirely on you because people with your background are typically"
                  "it's just totally obvious that you are"
                  "the simple reason you are not happy is that you are"
                  "no doubt in my mind that you are"
                  "people in your neighborhood think of you as"
                  "ask yourself, do you always want to be"
                  "you do have something to offer, but it's too bad you're"
                  "it's hard to be a leader when you're"
                  "relax, there are worse things to be than so"
                  "you are too hard on yourself. I can think of worse ways to be than"
                  "don't beat yourself up for being"
                  "congratulate yourself. Most people would give up if they looked in the mirror and thought:"
                  "you might as well forgive yourself for being so incredibly"
                  "of all humans who ever lived you are the most"])

(def person {:failure ["stupid" "vile" "fake" "soulless" "a bad friend" "childish" "selfish" "a fraud" "incapable of coming up with anything new" "unoriginal" "affected" "a loser" "foolish" "lumpy" "weak" "deeply flawed"  "dishonest" "hypocritical" "slow" "unable to finish anything" "badly prepared" "a terrible communicator"]
              :fear ["inadequate" "not funny" "not smart" "not cool" "not interesting" "boring" "impossible to like" "living in fear" "afraid" "pitiful" "terrified of everything" "perpetually scared" "full of self-hatred" "shaky" "nervous" "afflicted by nerves" "weak-kneed" "deficient" "lacking in empathy" "lacking in intelligence" "devoid of brilliance" "not even clever" "incapable" "incompetent" "unequal
    to the task" "barely tolerable" "deservedly alone" "so easily forgotten" "likely to die soon" "diseased-looking" "at risk of a fatal disease" "a burden on others" "awkward" "socially weird" "hard to talk with"]
              :loathing
              ["poison" "wasteful" "fraudulent"  "like garbage" "cheesy" "fake" "unworthy of saving" "irredeemable" "phony" "faux-intelligent" "a liar" "a cheater" "deviant" "cretinous" "evil-looking" "awful" "unsightly" "unloveable" "lazy" "stupid" "devoid of willpower" "unambitious" "third-rate" "z-list" "boring" "exhausting to know" "predictable" "whiny" "needy"]
              :appearance ["ugly" "hideous" "misshapen" "awkward" "oddly-proportioned" "weird" "strange" "repulsive" "monstrish" "disgusting" "smelly" "weird-faced" "unsexy" "untouchable" "crooked" "in posession of a weird nose" "undesirable" "strangely repulsive" "unkempt" "slobby" "weird-nosed" "sneaky-looking"]
              })

(def project
  {:failure ["waste of time" "stupid" "meaningless" "pointless" "empty"]})

(def person-keys (keys person))


(defn andjoin [s]
  (str (string/join ", " (butlast s))
    (cond
      (> (count s) 2) ", and "
      (= (count s) 2) " and ")
        (last s)))

(defn monster [k m]
  (andjoin (into #{}
             (take (+ 1 (rand-int 2)) (repeatedly
                       (fn [] (rand-nth ((rand-nth k) m))))))))

(defn sentence [s]
  (apply str (concat (string/upper-case (first s)) (rest s) ".")))

(defn indicator []
  (rand-nth indicators))

(defn description []
  (monster person-keys person))

(defn ps []
  (sentence (apply str (indicator) " " (description))))



