(ns i18n-word-guess.game)

(defn- code->re [code]
  (re-pattern (str "^"
                   (clojure.string/replace code #"\d+" #(str "\\D{" % "}"))
                   "$")))

(defn- check-guess [code guess]
  (let [prev-re (code->re code)]
    (re-seq prev-re guess)))

(defn- opaque-mask [word]
  (apply str (repeat (count word) \*)))

(defn- transparent-mask [word]
  (apply str (repeat (count word) \_)))

(defn- transparent? [mask]
  (not-any? #{\*} mask))

(defn- set-at [string idx chr]
  (apply str (assoc (vec string) idx chr)))

(defn- reveal [side mask]
  (let [edge (case side
               :front (.indexOf mask "*")
               :back  (.lastIndexOf mask "*"))]
    (if (neg? edge)
      mask
      (set-at mask edge \_))))

(defn- encode [mask word]
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
    [{:timestamp (java.util.Date.)
      :word      word
      :mask      mask
      :code      (encode mask word)
      :status    :start}]))

(defn step
  ([game new-guess]
   (step game new-guess (rand-nth [:front :back])))
  ([game new-guess reveal-side]
   (let [{:keys [word mask code status] :as prev-step} (last game)]
     (conj game
           (merge {:word      word
                   :guess     new-guess
                   :timestamp (java.util.Date.)}
                  (cond
                   (= new-guess word) {:status (if (= status :win) :over :win)
                                       :mask   (transparent-mask word)
                                       :code   word}
                   (some #{new-guess} (map :guess game)) {:status :repeat
                                                          :mask   mask
                                                          :code   code}
                   (check-guess code new-guess) (let [new-mask (reveal reveal-side mask)
                                                      new-mask (if (transparent? new-mask) mask new-mask)]
                                                  {:status :ok
                                                   :mask   new-mask
                                                   :code   (encode new-mask word)})
                   :else {:status :no-match
                          :mask   mask
                          :code   code}))))))

;;-----------------------------------------------------------------------------
;; Hints

(def vowels (into #{} "аеиоуыэюя"))

(defn- char-phon-class [c]
  (if (contains? vowels c) :vowel :consonant))

(defn- encode-hint [idx char-seq]
  (case (char-phon-class (first char-seq))
    :vowel (case (count char-seq)
             1 "A"
             2 "AO"
             3 "UAO")
    :consonant (case (count char-seq)
                 1 "T"
                 2 (if (pos? idx) "RT" "TR")
                 3 "STR"
                 4 "RNTK"
                 5 "FRNTK"
                 6 "PFRNTK")))


;; загвоздка -> TARTARTKA
;; баобаб -> TAOTAT
;; коэффициент -> TAORTATAORT

(defn word->phon [^String word]
  (->> (partition-by char-phon-class word)
       (map-indexed encode-hint)
       flatten
       (apply str)))

(defn code-hints [dict code]
  (->> dict
       (filter #(re-seq (code->re code) %))
       (map word->phon)
       distinct))
