(ns authorizer.rules
  (:require [clj-time.core :as t])
  (:require [clj-time.format :as f]))

(defn isAvailableLimit
  "checks whether account has sufficient limit"
  [availableLimit transactionAmount]
  (let [diff (- availableLimit transactionAmount)]
    (if (< diff 0)
      false
      true)))

(defn isTimeAllowed
  "checks if there is more than 3 transactions on a 2 minute interval"
  [transaction history]
  (println (f/parse "2019-02-13T10:00:00.000Z"))
  )

(defn isDoubledTransaction
  "checks if there is 2 similar transactions in a 2 minutes interval"
  [transaction history]
  )

(defn checkForViolations
  "checks for violations in json entry"
  [account jsonEntry history]
  (if-not (= nil (:account jsonEntry))
    (swap! (:violations account) conj "account-already-initialized")
    (if-not (= nil (:transaction jsonEntry))
      (if-not (:activeCard account)
        (swap! (:violations account) conj "card-not-active")
        (if-not (isAvailableLimit @(:availableLimit account) (:amount (:transaction jsonEntry)))
          (swap! (:violations account) conj "insufficient-limit")
          (if-not (isTimeAllowed (:transaction jsonEntry) history)
            (swap! (:violations account) conj "high-frequency-small-interval")
            (if (isDoubledTransaction (:transaction jsonEntry) history)
              (swap! (:violations account) conj "doubled-transaction"))))))))