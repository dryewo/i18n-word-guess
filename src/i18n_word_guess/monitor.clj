(ns i18n-word-guess.monitor
  (:require [i18n-word-guess [db :as db] [game :as impl]]
            [org.httpkit.server :refer [with-channel on-close send!]]
            [cheshire.core :as json]))

(def clients (atom {}))

(defn add-ws-connection [request]
  (with-channel request chan
    (swap! clients assoc chan true)
    (println chan " connected")
    (on-close chan (fn [status]
                    (swap! clients dissoc chan)
                    (println chan " disconnected. status: " status)))))

(defn prepare-step [{:keys [mask word] :as step}]
  (-> step
      (merge {:code (impl/encode mask word)})))

(defn start-monitor []
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
