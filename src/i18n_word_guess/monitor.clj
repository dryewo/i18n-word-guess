(ns i18n-word-guess.monitor
  (:require [i18n-word-guess [db :as db] [game :as impl]]
            [org.httpkit.server :refer [with-channel on-close send!]]
            [cheshire.core :as json]))

(def watchers (atom {}))

(defn add-ws-connection [request]
  (with-channel request chan
    (swap! watchers assoc chan true)
    (println chan " connected")
    (on-close chan (fn [status]
                    (swap! watchers dissoc chan)
                    (println chan " disconnected. status: " status)))))

(defn notify-watchers [data]
  (doseq [watcher @watchers]
    (send! (key watcher) (json/generate-string (merge data
                                                      {:watchers (count @watchers)}))
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
