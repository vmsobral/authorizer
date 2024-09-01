(ns authorizer.core
  (:require [authorizer.rules :as rules])
  (:require [authorizer.utils :as utils])
  (:import (java.io BufferedReader)))


(defn initAccount
  "initializes account"
  [account accountJson]
  (swap! account assoc :init true)
  (swap! account assoc :activeCard (:activeCard accountJson))
  (swap! account assoc :availableLimit (atom (:availableLimit accountJson)))
  (swap! account assoc :violations (atom []))
  )

(defn processTransaction
  "validates and processes transaction"
  [account jsonEntry history]
  (rules/checkForViolations account jsonEntry history)
  (if (= @(:violations account) [])
    (let [transaction (:transaction jsonEntry)]
      (swap! (:availableLimit account) - (:amount transaction))
      (swap! history conj transaction))))

(defn -main [& args]
  (def account (atom {:init false}))
  (def history (atom []))
  (with-open [rdr (BufferedReader. *in*)]
    (doseq [line (line-seq rdr)]

      (let [jsonEntry (utils/readCommand line)]
        (if-not (:init @account)
          (initAccount account (:account jsonEntry))
          (processTransaction @account jsonEntry history))
        (utils/printStatus @account)
        (reset! (:violations @account) [])
        ))))