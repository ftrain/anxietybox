(ns anxietybox.test.handler
  (:use clojure.test
        ring.mock.request  
        anxietybox.handler))

(deftest test-app
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= (:status response) 200))
      (is (= (:body response) "Hello World"))))
  
  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))

;; (defn bootstrap []
;;   (do (db/box-insert {:fullname "Paul Ford"
;;                     :email "ford@ftrain.com"
;;                     :project "The Secret Lives of Web Pages"
;;                     :confirm (db/uuid)
;;                     })))

;; (bootstrap)
