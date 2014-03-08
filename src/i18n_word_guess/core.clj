(ns i18n-word-guess.core
  (:require [i18n-word-guess.run :as run]
            [org.httpkit.server]
            [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [ring.swagger.schema :refer [defmodel]]
            [schema.core :as s]
            [clojure.java.io :as io]))

(defmodel Guess {:word String})

(defn wrap-cache-control [handler]
  (fn [request]
    (let [resp (handler request)
          headers (:headers resp)]
      (assoc-in resp [:headers "Cache-control"] "no-cache"))))

(defapi app
  compojure.api.middleware/public-resource-routes
  (swagger-ui "/api")
  (swagger-docs)
  (with-middleware [wrap-cache-control]
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
                        (GET* "/guess" [word]
                              :summary  "Попробовать угадать"
                              :query [guess Guess]
                              (ok (run/guess-game game_id word)))))))

(defn -main [& args]
  (let [port (or (first args) 3030)]
    (println "Starting on port" port)
    (org.httpkit.server/run-server app {:port (bigdec port)})))
