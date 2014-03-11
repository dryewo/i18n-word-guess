(ns i18n-word-guess.game)

(defn- code->re [code]
  (re-pattern (str "^"
                   (clojure.string/replace code #"\d+" #(str "\\D{" % "}"))
                   "$")))

(defn- check-guess-by-code [code guess]
  (let [prev-re (code->re code)]
    (re-seq prev-re guess)))

(defn- opaque-mask [word]
  (apply str (repeat (count word) \*)))

(defn- transparent-mask [word]
  (apply str (repeat (count word) \_)))

(defn- count-stars [mask]
  (count (filter #{\*} mask)))

(defn- transparent? [mask]
  (zero? (count-stars mask)))

(defn- set-at [string idx chr]
  (apply str (assoc (vec string) idx chr)))

(defn- reveal [side mask]
  (let [edge (case side
               :front (.indexOf     mask "*")
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

(defn encode2 [mask word]
  (->> (map #(if (= %1 \_) %2 \_) mask word)
       (partition-by identity)
       (map #(if (= (first %) \_) [\space (interpose \space %) \space] %))
       flatten
       (apply str)))

(defn initial-mask [word]
  (->> (opaque-mask word)
       (reveal :front)
       (reveal :back)))

(defn next-step [word mask new-guess
                 & {:keys [reveal-side] :or {reveal-side (rand-nth [:front :back])}}]
  (let [code (encode mask word)]
    (cond
     (= new-guess word) {:status :win
                         :mask   (transparent-mask word)}
     (check-guess-by-code code new-guess) {:status :ok
                                           :mask   (let [try-mask (reveal reveal-side mask)]
                                                     (if (transparent? try-mask) mask try-mask))}
     :else {:status :no-match
            :mask   mask})))

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
