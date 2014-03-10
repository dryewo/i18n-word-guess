(defproject i18n-word-guess "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [http-kit "2.1.17"]
                 [metosin/compojure-api "0.8.1"]
                 [metosin/ring-swagger-ui "2.0.10"]
                 [ring-mock "0.1.5"]
                 [com.taoensso/timbre "3.1.2"]
                 [org.clojure/java.jdbc "0.3.3"]
                 [postgresql "9.1-901.jdbc4"]
                 [javax.servlet/servlet-api "2.5"]]
  :main ^:skip-aot i18n-word-guess.core
  :min-lein-version "2.0.0"
  :target-path "target/%s"
  :uberjar-name "i18n-word-guess-standalone.jar"
  ;:profiles {:uberjar {:aot :all}}
  )
