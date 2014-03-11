(ns i18n-word-guess.monitor
  (:require [i18n-word-guess [db :as db] [game :as impl]]
            [org.httpkit.server :refer [with-channel on-close send!]]
            [cheshire.core :as json]))

(def watchers (atom {}))
(def players (atom {}))


(defn- add-ws-connection [store request]
  (with-channel request chan
    (swap! store assoc chan true)
    (println chan " connected")
    (on-close chan (fn [status]
                    (swap! store dissoc chan)
                    (println chan " disconnected. status: " status)))))

(defn add-watcher-connection [request]
  (add-ws-connection watchers request))

(defn add-player-connection [request]
  (add-ws-connection players request))

(defn notify-watchers [data]
  (doseq [watcher @watchers]
    (send! (key watcher) (json/generate-string (merge data
                                                      {:watchers (count @watchers)
                                                       :players (count @players)}))
           false)))

#_(defn prepare-step [{:keys [mask word] :as step}]
  (-> step
      (merge {:code (impl/encode mask word)})))

#_(defn start-monitor []
  (future (loop []
            (let [loop-start (java.util.Date.)]
              (Thread/sleep 5000)
              (let [new-steps (map prepare-step (db/get-steps-since loop-start))]
                (doseq [client @clients]
                  (send! (key client) (json/generate-string new-steps)
                         false))))
            (recur))))

;(def fut (start-monitor))
;(future-cancel fut)
