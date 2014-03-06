(ns i18n-word-guess.core
  (:require [i18n-word-guess.game :as impl]))

(defn -main [& args]
  (println "Hello, World!"))


;; -----------------------------------------------------------------------------

(use 'clojure.java.io)
(use 'clojure.pprint)

(defn load-words [file-name]
  (with-open [rdr (reader file-name)]
    (into [] (line-seq rdr))))

(def all-nouns (load-words (resource "nouns.txt")))
(def nouns (filter #(<= 5 (count %)) all-nouns))

(def game-id-seq (atom 0))

(defn gen-game-id []
  (swap! game-id-seq inc))


(def games (atom {}))

(defn game-message [{:keys [status guess]}]
  (case status
    :start "Начинайте"
    :ok "Продолжайте"
    :win "Вы победили"
    :repeat (str "Вариант \"" guess "\" уже был")
    :no-match (str "Вариант \"" guess "\" не подходит")
    :over "Игра закончена"
    ""))

(defn get-game
  #_([id]
   (get-game @games id))
  ([games-snapshot id]
   (let [game (games-snapshot id)
         {:keys [code status] :as last-step} (last game)]
     {:id id :code code :status status :message (game-message last-step)})))

(defn new-game []
  (let [game-id (gen-game-id)
        game (impl/create-game (rand-nth nouns))]
    (-> (swap! games assoc game-id game)
        (get-game game-id))))

(defn game-guess [game-id new-guess]
  (if (some #{new-guess} nouns)
    (-> (swap! games update-in [game-id] impl/step new-guess)
        (get-game game-id))
    (merge (get-game @games game-id)
           {:message (str "Слова \"" new-guess "\" нет в словаре")})))

;; -----------------------------------------------------------------------------

(new-game)
(get-game @games 17)
(game-guess 17 "ря")

(@games 17)

;; -----------------------------------------------------------------------------

(count all-nouns)
(count nouns)
(some #{"человек"} nouns)

(create-game (rand-nth nouns))

(-> (create-game "палка")
    (step "пиaла" :front)
    (step "падла" :front)
    pprint)
