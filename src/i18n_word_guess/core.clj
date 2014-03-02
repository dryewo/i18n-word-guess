(ns i18n-word-guess.core
  )

(defn -main [& args]
  (println "Hello, World!"))


;; -----------------------------------------------------------------------------

(use 'clojure.java.io)
(use 'clojure.pprint)

(defn load-words [file-name]
  (with-open [rdr (reader file-name)]
    (into [] (line-seq rdr))))

(defn check-guess [code guess]
  (let [prev-re (re-pattern (clojure.string/replace code #"\d+" #(str "\\D{" % "}")))]
    (re-seq prev-re guess)))

(defn opaque-mask [word]
  (apply str (repeat (count word) \*)))

(defn transparent-mask [word]
  (apply str (repeat (count word) \_)))

(defn transparent? [mask]
  (not-any? #{\*} mask))

(defn set-at [string idx chr]
  (apply str (assoc (vec string) idx chr)))

(defn reveal [side mask]
  (let [edge (case side
               :front (.indexOf mask "*")
               :back  (.lastIndexOf mask "*"))]
    (if (neg? edge)
      mask
      (set-at mask edge \_))))

(defn encode [mask word]
  {:pre [(= (count mask) (count word))]}
  (letfn [(apply-mask [m w]
                      (apply str (map #(if (= %1 \*) \* %2) m w)))
          (collapse-stars [mw]
                          (clojure.string/replace mw #"\*+" #(str (count %))))]
    (->> word
         (apply-mask mask)
         collapse-stars)))

(defn create-game [word]
  (let [mask (->> (opaque-mask word)
                  (reveal :front)
                  (reveal :back))]
    [{:word word
      :mask mask
      :code (encode mask word)}]))

(defn step
  ([game new-guess]
   (step game new-guess (rand-nth [:front :back])))
  ([game new-guess reveal-side]
   (let [{:keys [word mask code] :as prev-step} (last game)]
     (cond
      (= new-guess word) (conj game {:word word
                                     :guess new-guess
                                     :mask (transparent-mask word)
                                     :code word
                                     :status :win})
      (some #{new-guess} (map :guess game)) (conj game {:word word
                                                        :guess new-guess
                                                        :mask mask
                                                        :code code
                                                        :status :repeat})
      (check-guess code new-guess) (let [new-mask (reveal reveal-side mask)]
                                     (conj game {:word word
                                                 :guess new-guess
                                                 :mask (if (transparent? new-mask) mask new-mask)
                                                 :code (encode new-mask word)
                                                 :status :ok}))
      :else (conj game {:word word
                        :guess new-guess
                        :mask mask
                        :code code
                        :status :no-match})))))

;; -----------------------------------------------------------------------------

(def all-nouns (load-words (resource "nouns.txt")))
(def nouns (filter #(<= 5 (count %)) all-nouns))
(count all-nouns)
(count nouns)

(create-game (rand-nth nouns))

(-> (create-game "палка")
    (step "пиaла" :front)
    (step "падла" :front)
    pprint)
