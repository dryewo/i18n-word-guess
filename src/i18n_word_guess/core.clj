(ns i18n-word-guess.core
  (:require [i18n-word-guess.run :as run]
            [org.httpkit.server]
            [clojure.java.io :as io]
            [compojure.core :only [defroutes GET POST]]))

(defn app [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    {:text "hello HTTP!"}})

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
