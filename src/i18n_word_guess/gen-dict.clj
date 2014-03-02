(ns i18n-word-guess.gen-dict
  (:require [clojure.java.io :as io]))

(defn- parse-word [txt]
  (-> txt
      (clojure.string/split #"\t")
      (update-in [1] #(set (clojure.string/split % #"\s")))))

(defn- word-seq [file-reader]
  (->> (line-seq file-reader)
       (remove empty?)
       (map parse-word)))

(defn- filter-nouns [full-dict]
  (->> full-dict
       (filter #(clojure.set/subset? #{"сущ" "ед" "им"} (second %)))
       (map first)
       (filter #(not-any? #{\- \space} %))))

(defn gen [infile outfile]
 (with-open [rdr (io/reader infile)
             wrtr (io/writer outfile)]
   (binding [*out* wrtr]
     (doseq [n (filter-nouns (word-seq rdr))]
       (println n)))))
