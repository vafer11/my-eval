(ns my-eval.core-test
  (:require [clojure.test :refer :all]
            [my-eval.core :refer :all]))

(deftest my-eval-basic-stuff-test
  (testing "Testing my-eval with my-plus, my-times, and my-str functions"
    (is (= 3 (my-eval '(my-plus 1 2))))
    (is (= 5 (my-eval '(my-plus 1 (my-plus 2 2)))))
    (is (= 5 (my-eval '(my-plus :a (my-plus 2 2)))))
    (is (= "two plus two is: 4" (my-eval '(my-str "two plus two is: " (my-plus 2 2)))))
    (is (= "ten times ten is: 100" (my-eval '(my-str "ten times ten is: " (my-times 10 10)))))
    (is (= 2700 (my-eval '(my-times 3 3 3 10 10))))
    (is (= 191 (my-eval '(my-plus 2 (my-times 3 3 (my-plus :a :b (my-times :a :c (my-plus :c :c))))))))
    (is (= 729 (my-eval '(my-times :c :c :c :a (my-plus :c :c :c :c (my-plus :c :c (my-plus :c :c :c)))))))))

(deftest my-eval-my-map-test
  (testing "Testing my-eval with my-map function handling my-inc, my-plus, and my-times functions"
    (is (= [2 3 4] (my-eval '(my-map my-inc [1 2 3]))))
    (is (= [10 10 10] (my-eval '(my-map my-plus [5 5 5] [5 5 5]))))
    (is (= [25 25 25] (my-eval '(my-map my-times [5 5 5] [5 5 5]))))
    (is (= '("11" "22" "33") (my-eval '(my-map my-str [1 2 3] [1 2 3]))))
    (is (= [2 2 2] (my-eval '(my-map my-plus [:a :a :a] [:a :a :a]))))
    (is (= [1 1 1] (my-eval '(my-map my-times [:a :a :a] [:a :a :a]))))
    (is (= [5 3] (my-eval '(my-map my-inc [(my-plus 2 2 ) 2]))))
    (is (= [15 3] (my-eval '(my-map my-inc [(my-plus 2 2 (my-plus 2 2 (my-plus 2 (my-times 2 2)))) 2]))))))

(deftest my-eval-my-let-test
  (testing "Testing my-eval with my-let"
    (is (= 3 (my-eval '(my-let [a 1] (my-plus a 2)))))
    (is (= 3 (my-eval '(my-let [a 1] (my-let [b 2] (my-plus a b))))))
    (is (= 1500 (my-eval '(my-let [a 500] (my-let [b 500 c 500] (my-plus a b c))))))
    (is (= 3000 (my-eval '(my-let [a 10] (my-let [b 15] (my-let [c 20] (my-times a b c)))))))
    (is (= 15 (my-eval '(my-let [a 5] (my-plus a (my-let [b 5] (my-plus a b)))))))
    (is (= 0 (my-eval '(my-let [a 10 c 50] (my-times a c (my-let [a 0] (my-times a c)))))))
    (is (= 100 (my-eval '(my-times (my-let [a (my-plus 3 2) b (my-times 1 5)] (my-plus a b)) 10))))
    (is (= [5 6] (my-eval '(my-let [a 4 b 5] (my-map my-inc [a b])))))))

(deftest my-eval-my-fn-test
  (testing "Testing my-eval with my-fn function"
    (is (= 140 (my-eval '((my-fn [a b c] (my-plus a b c)) 50 50 (my-times 2 20)))))
    (is (= 290 (my-eval '((my-fn [a b] (my-let [c (my-times 20 10)] (my-plus a b c ))) 80 10))))
    (is (=  [20 21 22 23] (my-eval '((my-fn [l] (my-map my-inc l)) [19 20 21 22]))))
    (is (= [200 400] (my-eval '((my-fn [s1 s2] (map my-plus s1 s2)) [100 200] [100 200]))))
    (is (= "Hi Valentin" (my-eval '((my-fn [w1 space w2] (my-str w1 space w2)) "Hi" " " "Valentin"))))
    (is (= 19 (my-eval '(my-let [a 10] ((my-fn [b c] (my-plus a b c)) 4 5)))))
    (is (=  [100 400 900 1600]) (my-eval '((my-fn [t1 t2 t3 t4]
                                                 (my-map my-times [100 200 300 400] (list t1 t2 t3 t4))) 1 2 3 4)))))