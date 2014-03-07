(ns i18n-word-guess.core
  (:require [i18n-word-guess.run :as run]
            [org.httpkit.server]
            [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [ring.swagger.schema :refer [defmodel]]
            [schema.core :as s]
            [clojure.java.io :as io]))

(defmodel Guess {:word String})

(defapi app
  compojure.api.middleware/public-resource-routes
  (swagger-ui "/api")
  (swagger-docs)
  (swaggered "i18n-word-guess"
    :description "Слово угадай игра"
    (context "/game" []
      (GET* "/new" []
        :summary  "Создать новую игру"
        (ok (run/new-game)))
      (GET* "/all" []
        :summary  "Получить статусы всех игр"
        (ok (run/get-all-games)))
      (context "/:game_id" [game_id]
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

#_(
;; -----------------------------------------------------------------------------

(run/new-game)
(run/get-game 2)
(run/get-all-games)

(run/game-guess 1 "резерв")

 @run/games

(run/get-all-games)

;; -----------------------------------------------------------------------------

(count all-nouns)
(count nouns)
(some #{"человек"} nouns)

)
