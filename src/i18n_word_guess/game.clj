(ns i18n-word-guess.game)

(defn- check-guess [code guess]
  (let [prev-re (re-pattern (clojure.string/replace code #"\d+" #(str "\\D{" % "}")))]
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
      :word word
      :mask mask
      :code (encode mask word)
      :status :start}]))

(defn step
  ([game new-guess]
   (step game new-guess (rand-nth [:front :back])))
  ([game new-guess reveal-side]
   (let [{:keys [word mask code status] :as prev-step} (last game)]
     (conj game
           (merge {:timestamp (java.util.Date.)
                   :word word
                   :guess new-guess}
                  (cond
                   (= new-guess word) {:mask (transparent-mask word)
                                       :code word
                                       :status (if (= status :win) :over :win)}
                   (some #{new-guess} (map :guess game)) {:mask mask
                                                          :code code
                                                          :status :repeat}
                   (check-guess code new-guess) (let [new-mask (reveal reveal-side mask)
                                                      new-mask (if (transparent? new-mask) mask new-mask)]
                                                  {:mask new-mask
                                                   :code (encode new-mask word)
                                                   :status :ok})
                   :else {:mask mask
                          :code code
                          :status :no-match}))))))

#_(
(-> (create-game "пайка")
    (step "почка" :front)
    (step "палка" :back)
    (step "папка"))
)
