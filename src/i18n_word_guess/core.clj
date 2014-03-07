(ns i18n-word-guess.core
  (:require [i18n-word-guess.run :as run]
            [org.httpkit.server]
            [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [ring.swagger.schema :refer [defmodel]]
            [schema.core :as s]
            [clojure.java.io :as io]))

(defmodel Thingie {:id Long
                   :hot Boolean
                   :tag (s/enum :kikka :kukka)})

(defapi app
  (swagger-ui)
  (swagger-docs)
  (swaggered "i18n-word-guess"
    :description "Word guess game"
    (context "/game" []
      (GET* "/new" []
        :query    [thingie Thingie]
        :summary  "echos a thingie from query-params"
        (ok thingie))))) ;; here be coerced thingie

(defn -main [& args]
  (let [port (or (first args) 3030)]
    (println "Starting on port" port)
    (org.httpkit.server/run-server app {:port (bigdec port)})))


#_(
;; -----------------------------------------------------------------------------

(new-game)
(get-game @games 17)
(game-guess 17 "ря")

(@games 17)

;; -----------------------------------------------------------------------------

(count all-nouns)
(count nouns)
(some #{"человек"} nouns)

)
