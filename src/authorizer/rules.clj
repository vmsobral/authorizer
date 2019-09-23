(ns authorizer.rules)

(defn isAvailableLimit
  "checks whether account has sufficient limit"
  [availableLimit transactionAmount]
  (let [diff (- availableLimit transactionAmount)]
    (if (< diff 0)
      false
      true)))

(defn checkForViolations
  "checks for violations in json entry"
  [account jsonEntry]
  (if-not (= nil (:account jsonEntry))
    (swap! (:violations account) conj "account-already-initialized")
    (if-not (= nil (:transaction jsonEntry))
      (if-not (:activeCard account)
        (swap! (:violations account) conj "card-not-active")
      (if-not (isAvailableLimit @(:availableLimit account) (:amount (:transaction jsonEntry)))
        (swap! (:violations account) conj "insufficient-limit")))
      )))