(ns authorizer.core-test
  (:require [clojure.test :refer :all]
            [authorizer.core :refer :all]
            [authorizer.utils :refer :all]))

(deftest initAccount-test
  (testing "init account successfully."
    (let [account (atom {:init false})]
      (let [accountJson (readCommand "{ \"activeCard\": true, \"availableLimit\": 100 }")]
        (initAccount account accountJson)
        (is (= true (:init @account)))
        (is (= true (:activeCard @account)))
        (is (= 100 @(:availableLimit @account)))
        (is (= [] @(:violations @account)))))))