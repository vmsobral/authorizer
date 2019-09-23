(ns authorizer.core-test
  (:require [clojure.test :refer :all]
            [authorizer.core :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))


(defn teste []
  (def transactions (atom '()))
  (swap! transactions conj (:transaction (utils/readCommand "{ \"transaction\": { \"merchant\": \"Burger King\", \"amount\": 20, \"time\": \"2019-02-13T10:00:00.000Z\" }}")))
  (swap! transactions conj (:transaction (utils/readCommand "{ \"transaction\": { \"merchant\": \"Mercado Joia\", \"amount\":10, \"time\": \"2019-02-13T10:00:30.000Z\" }}")))
  (swap! transactions conj (:transaction (utils/readCommand "{ \"transaction\": { \"merchant\": \"Loja do Pe\", \"amount\": 20, \"time\": \"2019-02-13T10:01:00.000Z\" }}")))
  (let [transaction (:transaction (utils/readCommand "{ \"transaction\": { \"merchant\": \"Loja do Pe\", \"amount\": 30, \"time\": \"2019-02-13T10:02:30.000Z\" }}"))]
    ;(rules/isDoubledTransaction transaction @transactions)
    ))