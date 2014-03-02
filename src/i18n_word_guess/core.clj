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

(def all-nouns (load-words (resource "nouns.txt")))
(def nouns (filter #(<= 5 (count %) 8) all-nouns))
(count all-nouns)
(count nouns)

;; -----------------------------------------------------------------------------


(defn parse-word [txt]
  (-> txt
      (clojure.string/split #"\t")
      (update-in [1] #(set (clojure.string/split % #"\s")))))

(defn word-seq [file-reader]
  (->> (line-seq file-reader)
       (remove empty?)
       (map parse-word)))

(defn filter-nouns [full-dict]
  (->> full-dict
       (filter #(clojure.set/subset? #{"сущ" "ед" "им"} (second %)))
       (map first)
       (filter #(not-any? #{\- \space} %))))

(time
 (with-open [rdr (reader (resource "fulldict.txt"))
             wrtr (writer "nouns.txt")]
   (binding [*out* wrtr]
     (doseq [n (filter-nouns (word-seq rdr))]
       (println n)))))
