(ns authorizer.rules-test
  (:require [clojure.test :refer :all]
            [authorizer.utils :refer :all]
            [authorizer.rules :refer :all]))

(deftest removeOldTransactions-test
  (testing "should remove only the first transaction from the history"
    (let [transaction (readCommand "{ \"merchant\": \"JV Suplementos\", \"amount\": 30, \"time\": \"2019-02-13T10:02:30.000Z\" }")]
      (let [transactions (atom [
                                (readCommand "{ \"merchant\": \"Ivans\", \"amount\": 50, \"time\": \"2019-02-13T10:00:00.000Z\" }")
                                (readCommand "{ \"merchant\": \"Mercado Paizao\", \"amount\":10, \"time\": \"2019-02-13T10:00:30.000Z\" }")
                                ])]
        (removeOldTransactions transaction transactions)
        (is (= 1 (count @transactions)))
        (is (= "Mercado Paizao" (:merchant (first @transactions))))))))

(deftest notRemoveOldTransactions-test
  (testing "should not remove any element"
    (let [transaction (readCommand "{ \"merchant\": \"JV Suplementos\", \"amount\": 30, \"time\": \"2019-02-13T10:02:30.000Z\" }")]
      (let [transactions (atom [
                                (readCommand "{ \"merchant\": \"Ivans\", \"amount\": 50, \"time\": \"2019-02-13T10:01:00.000Z\" }")
                                (readCommand "{ \"merchant\": \"Mercado Paizao\", \"amount\":10, \"time\": \"2019-02-13T10:01:30.000Z\" }")
                                ])]
        (removeOldTransactions transaction transactions)
        (is (= 2 (count @transactions)))
        (is (= "Ivans" (:merchant (first @transactions))))))))

(deftest isAvailableLimit-test
  (testing "test if limit available is sufficient"
    (is (= true (isAvailableLimit 100 50)))
    (is (= false (isAvailableLimit 10 50)))))

(deftest isTimeAllowed-test
  (testing "test if number of transactions within the time limit is allowed"
    (is (= true (isTimeAllowed ["transaction1" "transaction2"])))
    (is (= false (isTimeAllowed ["transaction1" "transaction2" "transaction3" "transaction4"])))))

(deftest isDoubledTransaction-test
  (testing "test if transaction is doubled in the last 2 minutes"
    (let [transaction (readCommand "{ \"merchant\": \"Birno's\", \"amount\": 15, \"time\": \"2019-02-13T10:02:30.000Z\" }")]
      (let [transactions [
                          (readCommand "{ \"merchant\": \"Birno's\", \"amount\": 50, \"time\": \"2019-02-13T10:01:00.000Z\" }")
                          (readCommand "{ \"merchant\": \"Amazingoró\", \"amount\":10, \"time\": \"2019-02-13T10:01:30.000Z\" }")
                          ]]
        (is (= nil (isDoubledTransaction transaction transactions))))
      (let [transactions [
                          (readCommand "{ \"merchant\": \"Birno's\", \"amount\": 15, \"time\": \"2019-02-13T10:01:00.000Z\" }")
                          (readCommand "{ \"merchant\": \"Amazingoró\", \"amount\":10, \"time\": \"2019-02-13T10:01:30.000Z\" }")
                          ]]
        (is (= true (isDoubledTransaction transaction transactions)))))))

(deftest checkForInsufficientLimit-test
  (testing "test account has insufficient limit"
    (let [account {:init true :activeCard true :availableLimit (atom 100) :violations (atom [])}]
      (let [jsonEntry (readCommand "{ \"transaction\": { \"merchant\": \"Birno's\", \"amount\": 105, \"time\": \"2019-02-13T10:02:30.000Z\" } }")]
        (let [transactions (atom [])]
          (checkForViolations account jsonEntry transactions)
          (is (= ["insufficient-limit"] @(:violations account))))))))

(deftest checkForAccountAlreadyInitialized-test
  (testing "test if account has already been initialized"
    (let [account {:init true :activeCard true :availableLimit (atom 100) :violations (atom [])}]
      (let [jsonEntry (readCommand "{ \"account\": { \"activeCard\": true, \"availableLimit\": 100 } }")]
        (let [transactions (atom [])]
          (checkForViolations account jsonEntry transactions)
          (is (= ["account-already-initialized"] @(:violations account))))))))

(deftest checkForCardNotActive-test
  (testing "test if account card is active"
    (let [account {:init true :activeCard false :availableLimit (atom 100) :violations (atom [])}]
      (let [jsonEntry (readCommand "{ \"transaction\": { \"merchant\": \"Ivans\", \"amount\": 105, \"time\": \"2019-02-13T10:02:30.000Z\" } }")]
        (let [transactions (atom [])]
          (checkForViolations account jsonEntry transactions)
          (is (= ["card-not-active"] @(:violations account))))))))

(deftest checkForHighFrequencySmallInterval-test
  (testing "test if there is too many transactions in a 2 minutes interval"
    (let [account {:init true :activeCard true :availableLimit (atom 100) :violations (atom [])}]
      (let [jsonEntry (readCommand "{ \"transaction\": { \"merchant\": \"Birno's\", \"amount\": 10, \"time\": \"2019-02-13T10:02:30.000Z\" } }")]
        (let [transactions (atom [
                                  (readCommand "{ \"merchant\": \"JV Suplementos\", \"amount\": 15, \"time\": \"2019-02-13T10:01:00.000Z\" }")
                                  (readCommand "{ \"merchant\": \"Orti Zone\", \"amount\":70, \"time\": \"2019-02-13T10:01:30.000Z\" }")
                                  (readCommand "{ \"merchant\": \"Bar do Freddy\", \"amount\":50, \"time\": \"2019-02-13T10:01:45.000Z\" }")
                                  (readCommand "{ \"merchant\": \"Amazingoró\", \"amount\":10, \"time\": \"2019-02-13T10:02:10.000Z\" }")
                                  ])]
          (checkForViolations account jsonEntry transactions)
          (is (= ["high-frequency-small-interval"] @(:violations account))))))))

(deftest checkForDoubledTransaction-test
  (testing "test if there is an identical transaction in a 2 minutes interval"
    (let [account {:init true :activeCard true :availableLimit (atom 100) :violations (atom [])}]
      (let [jsonEntry (readCommand "{ \"transaction\": { \"merchant\": \"Restaurante do Panqueca\", \"amount\": 50, \"time\": \"2019-02-13T10:02:30.000Z\" } }")]
        (let [transactions (atom [
                                  (readCommand "{ \"merchant\": \"Restaurante do Panqueca\", \"amount\":50, \"time\": \"2019-02-13T10:01:30.000Z\" }")
                                  ])]
          (checkForViolations account jsonEntry transactions)
          (is (= ["doubled-transaction"] @(:violations account))))))))

(deftest checkForNoViolations-test
  (testing "test if there is not a single violation"
    (let [account {:init true :activeCard true :availableLimit (atom 100) :violations (atom [])}]
      (let [jsonEntry (readCommand "{ \"transaction\": { \"merchant\": \"Restaurante do Panqueca\", \"amount\": 50, \"time\": \"2019-02-13T10:02:30.000Z\" } }")]
        (let [transactions (atom [
                                  (readCommand "{ \"merchant\": \"JV Suplementos\", \"amount\": 15, \"time\": \"2019-02-13T10:01:00.000Z\" }")
                                  (readCommand "{ \"merchant\": \"Orti Zone\", \"amount\":70, \"time\": \"2019-02-13T10:01:30.000Z\" }")
                                  ])]
          (checkForViolations account jsonEntry transactions)
          (is (= [] @(:violations account))))))))