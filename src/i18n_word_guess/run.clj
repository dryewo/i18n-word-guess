(ns i18n-word-guess.run
  (:require [i18n-word-guess [game :as impl]
                             [db :as db]
                             [monitor :as monitor]]
            [clojure.java.io :as io]))

;;-----------------------------------------------------------------------------
;; Dictionary

(defn- load-words [file-name]
  (with-open [rdr (io/reader file-name)]
    (into [] (line-seq rdr))))

(def questionable-nouns (filter #(<= 5 (count %) 10)
                   (load-words (io/resource "nouns.txt"))))

(def all-nouns (load-words (io/resource "nouns-full.txt")))

;(count questionable-nouns)
;(count all-nouns)

;;-----------------------------------------------------------------------------
;; Game routine

(defn- game-message [{:keys [status guess]}]
  (case status
    :start "Начинайте"
    :ok "Продолжайте"
    :win "Вы победили"
    :repeat (str "Вариант \"" guess "\" уже был")
    :no-match (str "Вариант \"" guess "\" не подходит")
;    :over "Игра уже закончена"
    :not-in-dict  (str "Слова \"" guess "\" нет в словаре")
    ""))

(defn- format-step [{:keys [word mask] :as step}]
  (merge (select-keys step [:game_id
                            :guess
                            :mask
                            :status
                            :creation_date])
         {:code    (impl/encode  mask word)
          :code2   (impl/encode2 mask word)
          :message (game-message step)}))

(defn- get-game-cur [id]
  (->> (db/get-game-cur id)
       format-step))

(defn get-game-history [id]
  (->> (db/get-game-history id)
       (map format-step)))

(defn new-game! [& [{:keys [session] :as ctx}]]
  (let [word               (rand-nth questionable-nouns)
        [game-rec]         (db/insert-game! {:word word})
        game-id            (:id game-rec)
        [initial-step-rec] (db/insert-step! {:game_id game-id
                                             :session session
                                             :mask    (impl/initial-mask word)
                                             :status  :start})]
    (get-game-cur game-id)))

(defn guess-game! [game-id new-guess
                   & [{:keys [session] :as ctx}]]
  (let [lower-guess (clojure.string/lower-case new-guess)
        prev-guesses (map :guess (db/get-game-history game-id))
        {:keys [word mask status]} (db/get-game-cur game-id)]
    (when-not (= (keyword status) :win)
      (db/insert-step!
       (merge {:game_id   game-id
               :session   session
               :guess     new-guess}
              (cond
               (some #{lower-guess} prev-guesses)  {:status :repeat
                                                    :mask   mask}
               (not-any? #{lower-guess} all-nouns) {:status :not-in-dict
                                                    :mask   mask}
               :else (impl/next-step word mask lower-guess)))))
    (let [res (get-game-cur game-id)]
      (monitor/notify-watchers {:timestamp (java.util.Date.)
                                :code      (impl/encode mask word)
                                :code2     (impl/encode2 mask word)
                                :guess     new-guess
                                :message   (:message res)})
      res)))

;(get-game-cur 26)
;(new-game! {:session "65453"})
;(guess-game! 29 "сифен")
;(get-game-history 29)

(defn get-hints [code]
  (impl/code-hints all-nouns code))

;; (distinct (map impl/word->phon questionable-nouns))
