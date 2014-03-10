(ns i18n-word-guess.db
  (:require [clojure.java.jdbc :as jdbc]))

(def conn (or (System/getenv "DATABASE_URL")
              "postgresql://postgres:password@localhost:5432/i18n-games"))

(def games-ddl (jdbc/create-table-ddl :games
                       [:id :serial "PRIMARY KEY"]
                       [:word :varchar "NOT NULL"]
                       [:creation_date :timestamp
                        "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]))

(def steps-ddl (jdbc/create-table-ddl :steps
                       [:id :serial "PRIMARY KEY"]
                       [:game_id :integer]
                       [:session :varchar]
                       [:guess :varchar]
                       [:mask :varchar]
                       [:code :varchar]
                       [:status :varchar]
                       [:creation_date :timestamp
                        "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]))

(defn- exists? []
  (-> (jdbc/query conn
                  [(str "select count(*) from information_schema.tables "
                        "where table_name in ('games', 'steps')")])
      first :count (= 2)))

(defn init []
  (when-not (exists?)
    (print "Creating tables...") (flush)
    (jdbc/db-do-commands conn
                         "drop table if exists games"
                         "drop table if exists steps"
                         games-ddl
                         steps-ddl)
    (println " done")))

(defn insert-game! [word]
  (jdbc/insert! conn :games {:word word}))
