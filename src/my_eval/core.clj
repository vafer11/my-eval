(ns my-eval.core)

(defn my-plus [& inputs] (apply + inputs))

(defn my-str [& inputs] (apply str inputs))

(defn my-times [& inputs] (apply * inputs))

(defn my-inc [input] (inc input))

(defn my-map [func & inputs] (apply map func inputs))

(defn dispatch-eval-type [exp]
  (cond
    (symbol? exp) :eval-symbol
    (keyword? exp) :eval-keyword
    (vector? exp) :eval-vector
    (or (number? exp) (string? exp)) :eval-number-string
    (list? exp) :eval-list))

(defmulti my-eval dispatch-eval-type)

(defmethod my-eval :eval-symbol [symbol]
    (ns-resolve 'my-eval.core symbol))

(defmethod my-eval :eval-keyword [exp]
  ({:a 1 :b 2 :c 3} exp))

(defmethod my-eval :eval-number-string [exp]
  exp)

(defmethod my-eval :eval-vector [exp]
  (map my-eval exp))

(defmethod my-eval :eval-list [exp]
  (let [evaluated-elem (my-map my-eval exp)
        [func & args] evaluated-elem
        [my-map map-fun & map-args] evaluated-elem]
    (cond
      (and (var? my-map) (var? map-fun)) (apply my-map map-fun map-args)
      (var? func) (apply func args)
      :else evaluated-elem)))