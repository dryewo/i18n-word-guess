(ns i18n-word-guess.gen-dict
  (:require [clojure.java.io :as io]
            [clojure.set]))

; fulldict.txt
(defn- parse-word1 [txt]
  (let [[word tags id] (clojure.string/split txt #"\t")]
    {:word word
     :tags (set (clojure.string/split tags #"\s"))
     :id   (bigdec id)}))

(defrecord Rec1 [word tags id])

(defn- parse-word1-opt [txt]
  (let [[word tags id] (clojure.string/split txt #"\t")]
    (Rec1. word
           (set (clojure.string/split tags #"\s"))
           0 #_(bigdec id))))

#_(defn- parse-word2 [txt]
  (-> txt
      (clojure.string/split #"\t")
      (update-in [2] #(set (clojure.string/split % #"\s")))))

; Частотность_лемм_по_убыванию
(defn- parse-word3 [txt]
  (let [[_ occurences coverage word tags id] (clojure.string/split txt #"\t")]
    {:occurences (bigdec occurences)
     :coverage   (bigdec coverage)
     :word       word
     :tags       (set (clojure.string/split tags #"\s"))
     :id         (bigdec id)}))

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

(defn convert1 [infile outfile]
  (convert-impl infile outfile
             parse-word1-opt
             (every-pred noun-base-form? monoword?)
             :word))


;; Загрузка в память списка слов вместе с параметрами иx
;(load3 (io/resource "dict-freq-desc.txt"))

;; Генерация файла со списком слов
;(time (convert3 (io/resource "dict-freq-desc.txt") "nouns.txt"))
;(time (convert1 (io/resource "fulldict.txt") "nouns-full.txt"))
