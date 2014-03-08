(ns i18n-word-guess.run
  (:require [i18n-word-guess.game :as impl]
            [clojure.java.io :as io]))

;;-----------------------------------------------------------------------------
;; ID generation

(def game-id-seq (atom 0))

(defn gen-game-id []
  (str (swap! game-id-seq inc)))

;;-----------------------------------------------------------------------------
;; Dictionary

(defn- load-words [file-name]
  (with-open [rdr (io/reader file-name)]
    (into [] (line-seq rdr))))

(def questionable-nouns (filter #(<= 5 (count %))
                   (load-words (io/resource "nouns.txt"))))

(def all-nouns (load-words (io/resource "nouns-full.txt")))

;(count questionable-nouns)
;(count all-nouns)

;;-----------------------------------------------------------------------------
;; Game routine

(def games (atom {}))

(defn- game-message [{:keys [status guess]}]
  (case status
    :start "Начинайте"
    :ok "Продолжайте"
    :win "Вы победили"
    :repeat (str "Вариант \"" guess "\" уже был")
    :no-match (str "Вариант \"" guess "\" не подходит")
    :over "Игра уже закончена"
    ""))

(defn format-game [id game]
  (let [last-step (last game)]
    (merge (select-keys last-step [:code :status :timestamp])
           {:id id
            :message (game-message last-step)})))

(defn get-game
  ([id]
   (get-game @games id))
  ([games-snapshot id]
   (format-game id (games-snapshot id))))

(defn get-all-games
  ([]
   (get-all-games @games))
  ([games-snapshot]
   (map #(format-game (key %) (val %)) @games)))

(defn new-game []
  (let [game-id (gen-game-id)
        game (impl/create-game (rand-nth questionable-nouns))]
    (-> (swap! games assoc game-id game)
        (get-game game-id))))

(defn guess-game [game-id new-guess]
  (let [lower-guess (clojure.string/lower-case new-guess)]
    (if (some #{lower-guess} all-nouns)
      (-> (swap! games update-in [game-id] impl/step lower-guess)
          (get-game game-id))
      (merge (get-game @games game-id)
             {:message (str "Слова \"" new-guess "\" нет в словаре")}))))
