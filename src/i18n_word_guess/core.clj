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

