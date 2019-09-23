(ns authorizer.core
  (:require [authorizer.rules :as rules])
  (:require [authorizer.utils :as utils]))

(defn -main []
  (let [accountJson (:account (utils/readCommand (read-line)))]
    (def account
      {:activeCard     (accountJson :activeCard)
       :availableLimit (atom (accountJson :availableLimit))
       :violations     (atom [])}))
  (utils/printStatus account)

  (loop [entry (read-line)]
    (if-not (= ":done" entry)
      (let [jsonEntry (utils/readCommand entry)]
        (rules/checkForViolations account jsonEntry)
        (if (= @(:violations account) [])
          (swap! (:availableLimit account) - (:amount (:transaction jsonEntry))))
        (utils/printStatus account)
        (recur (read-line))))))