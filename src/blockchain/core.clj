(ns blockchain.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.walk :refer [keywordize-keys]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.util.response :as resp]
            [blockchain.init :refer [chain nodes]]
            [blockchain.worker :refer [forge-new-block append-to-chain]]))

(defn generate-response [response]
  (if (integer? (:status response))
    (let [body (:body response)
          status (:status response)]
      {:body body :status status})
    (resp/response response)))

(defroutes app-routes
  (GET "/" [] (resp/response @chain))
  (GET "/nodes" [] (resp/response @nodes))
  (GET "/mine" []
       (let [new-block (forge-new-block)]
         (append-to-chain new-block)
         (generate-response {:body new-block :status 201})))
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (wrap-json-body)
      (wrap-json-response)))
