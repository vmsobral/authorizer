(ns authorizer.utils
  (:require [clojure.data.json :as json]))

(defn readCommand
  "reads json command"
  [command]
  (json/read-str command :key-fn keyword))

(defn printStatus
  "prints account status"
  [account]
  (println (str "{ \"account\": { \"activeCard\": " (:activeCard account) ", \"availableLimit\": " @(:availableLimit account) " }, \"violations\": " @(:violations account) " }")))