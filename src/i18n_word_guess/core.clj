(ns i18n-word-guess.core
  (:gen-class)
  (:require [i18n-word-guess [run :as run]
                             [db :as db]]
            [org.httpkit.server]
            [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer [ok
                                             resource-response]]
            [ring.swagger.schema :refer [defmodel]]
            [schema.core :as s]
            [clojure.java.io :as io]
            [taoensso.timbre :as log]))

(log/merge-config! {:timestamp-pattern "yyyy-MM-dd HH:mm:ss ZZ"})

(defmodel Guess   {:word String})
(defmodel GetHint {:code String})

(defn wrap-cache-control [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers "Cache-control"] "no-cache"))))

(defn wrap-log [handler]
  (fn [request]
    (let [req-id (apply str (take 4 (repeatedly #(rand-nth "ABCDEF0123456789"))))]
      (log/info req-id
                (->> (:request-method request) name clojure.string/upper-case)
                (:uri request)
                (:params request))
      (let [response (handler request)]
        (log/info req-id
                  (:status response)
                  (:body response))
        response))))

(defapi app
  (swagger-ui "/api")
  (swagger-docs)
  (with-middleware [wrap-log wrap-cache-control]
    (GET* "/" []
          (resource-response "index.html" {:root "public"}))
    (swaggered "i18n-word-guess"
               :description "Слово угадай игра"
               (context "/games" []
                        (GET* "/new" []
                              :summary  "Создать новую игру"
                              (ok (run/new-game)))
                        (GET* "/all" []
                              :summary  "Получить статусы всех игр"
                              (ok (run/get-all-games))))
               (context "/game/:game_id" [game_id]
                        (GET* "/" []
                              :summary  "Получить статус игры по ID"
                              (ok (run/get-game game_id)))
                        (POST* "/guess" []
                               :summary  "Попробовать угадать"
                               :body [guess Guess]
                               (ok (run/guess-game game_id (:word guess)))))
               (GET* "/hints" [code]
                     :summary  "Получить подсказку"
                     :query [getHint GetHint]
                     (ok (run/get-hints code))))
    compojure.api.middleware/public-resource-routes
    (compojure.route/not-found "<h1>NO.</h1>")))

(defn -main [& args]
  (let [port (or (first args) 3030)]
    (db/init)
    (println "Starting on port" port)
    (org.httpkit.server/run-server #'app {:port (bigdec port)})))
