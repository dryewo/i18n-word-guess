(ns i18n-word-guess.db
  (:require [clojure.java.jdbc :as jdbc]))

(def conn (or (System/getenv "DATABASE_URL")
              "postgresql://postgres:password@localhost:5432/i18n-games"))

(defn- status->kw [m]
  (update-in m [:status] keyword))

(defn- status->name [m]
  (update-in m [:status] name))

;;-----------------------------------------------------------------------------
;; Inserts

(defn insert-game! [{:keys [word]
                     :as game}]
  (jdbc/insert! conn :games game))

(defn insert-step! [{:keys [game-id session guess mask code status]
                     :as step}]
  (jdbc/insert! conn :steps
                (status->name step)))

;;-----------------------------------------------------------------------------
;; Selects

(def game-steps-select
  "SELECT * FROM game_steps WHERE game_id = ? ORDER BY creation_date DESC")

(defn get-game-cur [id]
  (-> (jdbc/query conn [(str game-steps-select " LIMIT 1") id])
      first
      status->kw))

(defn get-game-history [id]
  (->> (jdbc/query conn [game-steps-select id])
       (map status->kw)))
