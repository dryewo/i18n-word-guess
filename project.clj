(defproject i18n-word-guess "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]]
  :main ^:skip-aot i18n-word-guess.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
