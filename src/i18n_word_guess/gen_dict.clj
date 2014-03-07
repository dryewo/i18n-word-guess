(ns i18n-word-guess.gen-dict
  (:require [clojure.java.io :as io]
            [clojure.set]))

(defn- parse-word [txt]
  (-> txt
      (clojure.string/split #"\t")
      (update-in [1] #(set (clojure.string/split % #"\s")))))

(defn- parse-word2 [txt]
  (-> txt
      (clojure.string/split #"\t")
      (update-in [2] #(set (clojure.string/split % #"\s")))))

; Частотность_лемм_по_убыванию
(defn- parse-word3 [txt]
  (let [parts (clojure.string/split txt #"\t")]
    {:occurences (bigdec (parts 1))
     :coverage (bigdec (parts 2))
     :word (parts 3)
     :tags (set (clojure.string/split (parts 4) #"\s"))
     :id (bigdec (parts 5))}))

(defn- read-lines [file-reader]
  (->> (line-seq file-reader)
       (remove empty?)))

(defn- noun-base-form? [word]
  (clojure.set/subset? #{"сущ" "ед" "им"} (:tags word)))

(defn- monoword? [word]
  (not-any? #{\- \space} (:word word)))

(defn- convert-impl [infile outfile parse-fn select-fn view-fn]
  (with-open [rdr (io/reader infile)]
    (with-open [wrtr (io/writer outfile)]
      (binding [*out* wrtr]
        (doseq [w (->> (read-lines rdr)
                       (map parse-fn)
                       (filter select-fn)
                       (map view-fn))]
          (println w))))))

(defn- load-impl [infile parse-fn select-fn view-fn]
  (with-open [rdr (io/reader infile)]
    (into [] (->> (read-lines rdr)
                  (map parse-fn)
                  (filter select-fn)
                  (map view-fn)))))

(defn load3 [infile]
  (load-impl infile
             parse-word3
             (every-pred noun-base-form? monoword? #(< (:coverage %) 90))
             identity))

(defn convert3 [infile outfile]
  (convert-impl infile outfile
             parse-word3
             (every-pred noun-base-form? monoword? #(< (:coverage %) 90))
             :word))


;; Загрузка в память списка слов вместе с параметрами иx
;(load3 (io/resource "dict-freq-desc.txt"))

;; Генерация файла со списком слов
;(convert3 (io/resource "dict-freq-desc.txt") "nouns.txt")
