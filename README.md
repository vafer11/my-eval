# my-eval

A practical exercise to code part of the implementation of eval function

## Usage

```clojure
;; Evaluating basic stuff ...
(my-eval 1)                                                ; 1

(my-eval '(1 2 3))                                         ; '(1 2 3)

(my-eval '(my-plus 1 2))                                   ; 3

(my-eval '(my-plus :a (my-plus 2 2)))                      ; 5

(my-eval '(my-str "two plus two is: " (my-plus 2 2)))      ; "two plus two is: 4"

(my-eval '(my-times 3 3 3 10 10))                          ; 2700

;; Evaluating my-map function ...
(my-eval '(my-map my-inc [1 2 3]))                         ; [2 3 4]

(my-eval '(my-map my-str [1 2 3] [1 2 3]))                 ; '("11" "22" "33")

(my-eval '(my-map my-inc [(my-plus 2 2 ) 2]))              ; [5 5]

(my-eval '(my-map my-plus [5 5 5] [5 5 5]))                ; [10 10 10]
```

## License

Copyright © 2020 Valentín Fernández

This program and the accompanying materials are made available under the terms of the Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0.
