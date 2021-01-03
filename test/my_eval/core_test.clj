(ns my-eval.core-test
  (:require [clojure.test :refer :all]
            [my-eval.core :refer :all]))
(deftest test-my-eval-p1
  (testing "Testing my-eval with my-plus, my-times, and my-str function"
    (is (= 3 (my-eval '(my-plus 1 2))))
    (is (= 5 (my-eval '(my-plus 1 (my-plus 2 2)))))
    (is (= 5 (my-eval '(my-plus :a (my-plus 2 2)))))
    (is (= "two plus two is: 4" (my-eval '(my-str "two plus two is: " (my-plus 2 2)))))
    (is (= "ten times ten is: 100" (my-eval '(my-str "ten times ten is: " (my-times 10 10)))))
    (is (= 2700 (my-eval '(my-times 3 3 3 10 10))))
    (is (= 191 (my-eval '(my-plus 2 (my-times 3 3 (my-plus :a :b (my-times :a :c (my-plus :c :c))))))))
    (is (= 729 (my-eval '(my-times :c :c :c :a (my-plus :c :c :c :c (my-plus :c :c (my-plus :c :c :c)))))))))

(deftest test-my-eval-p2
  (testing "Testing my-eval with my-map handling my-inc, my-plus, and my-times functions"
    (is (= [2 3 4] (my-eval '(my-map my-inc [1 2 3]))))
    (is (= [10 10 10] (my-eval '(my-map my-plus [5 5 5] [5 5 5]))))
    (is (= [25 25 25] (my-eval '(my-map my-times [5 5 5] [5 5 5]))))
    (is (= '("11" "22" "33") (my-eval '(my-map my-str [1 2 3] [1 2 3]))))
    (is (= [2 2 2] (my-eval '(my-map my-plus [:a :a :a] [:a :a :a]))))
    (is (= [1 1 1] (my-eval '(my-map my-times [:a :a :a] [:a :a :a]))))
    (is (= [5 3] (my-eval '(my-map my-inc [(my-plus 2 2 ) 2]))))
    (is (= [15 3] (my-eval '(my-map my-inc [(my-plus 2 2 (my-plus 2 2 (my-plus 2 (my-times 2 2)))) 2]))))))