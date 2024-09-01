(ns authorizer.rules
  (:require [clj-time.core :as t])
  (:require [clj-time.format :as f]))

(defn removeOldTransactions
  "removes old transactions from history"
  [transaction history]
  (swap! history (fn [history]
                   (remove (fn [historyTransaction]
                             (t/before? (f/parse (:time historyTransaction)) (t/minus (f/parse (:time transaction)) (t/minutes 2)))) history))))

(defn isAvailableLimit
  "checks whether account has sufficient limit"
  [availableLimit transactionAmount]
  (let [diff (- availableLimit transactionAmount)]
    (if (< diff 0)
      false
      true)))

(defn isTimeAllowed
  "checks if there is more than 3 transactions on a 2 minute interval (including actual)"
  [twoMinutesHistory]
  (< (count twoMinutesHistory) 3))

(defn isDoubledTransaction
  "checks if there is 2 similar transactions in a 2 minutes interval"
  [transaction history]
  (some (fn [historyTransaction]
          (and (= (:merchant transaction) (:merchant historyTransaction)) (= (:amount transaction) (:amount historyTransaction)))) history))

(defn checkForViolations
  "checks for violations in json entry"
  [account jsonEntry history]
  (if (:init account)
    (if-not (= nil (:account jsonEntry))
      (swap! (:violations account) conj "account-already-initialized")
      (if-not (= nil (:transaction jsonEntry))
        (let [transaction (:transaction jsonEntry)]
          (removeOldTransactions transaction history)
          (if-not (:activeCard account)
            (swap! (:violations account) conj "card-not-active")
            (if-not (isAvailableLimit @(:availableLimit account) (:amount transaction))
              (swap! (:violations account) conj "insufficient-limit")
              (if-not (isTimeAllowed @history)
                (swap! (:violations account) conj "high-frequency-small-interval")
                (if (isDoubledTransaction transaction @history)
                  (swap! (:violations account) conj "doubled-transaction"))))))))))