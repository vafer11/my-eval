(ns my-eval.core)

;; Aux Functions:
(defn get-keyword [symbol] (keyword (str symbol)))

(defn get-map-from-vector [args]
  (->> args
       (mapv #(if (symbol? %1) (get-keyword %1) %1))
       (apply hash-map)))

(defn merge-meta [exp meta]
  (let [not-string-number-or-keyword (not (or (string? exp) (number? exp) (keyword? exp)))]
    (if not-string-number-or-keyword (vary-meta exp merge meta) exp)))

;; Implemented functions:

(defn my-plus [& inputs] (apply + inputs))

(defn my-str [& inputs] (apply str inputs))

(defn my-times [& inputs] (apply * inputs))

(defn my-inc [input] (inc input))

(defn my-map [func & inputs] (apply map func inputs))


(defn my-let
  "It just returns the body with extra meta data.
   It adds the let bindings to :let-binding key, into the body's meta data"
  [args body]
  (let [let-binding (get-map-from-vector args)]
    (if (:let-binding (meta body))
      (vary-meta body #(update %1 :let-binding merge %2) let-binding)
      (merge-meta body {:let-binding let-binding}))))

(defn my-fn [args body params]
  (let [interleave-args-params (->> (interleave args params)
                                    (get-map-from-vector))]
    (if (:let-binding (meta body))
      (vary-meta body #(update %1 :let-binding merge interleave-args-params))
      (merge-meta body {:let-binding interleave-args-params}))))

(defn dispatch-eval-type [exp]
  (cond
    (symbol? exp) :eval-symbol
    (keyword? exp) :eval-keyword
    (vector? exp) :eval-vector
    (or (number? exp) (string? exp)) :eval-number-string
    (and (list? exp) (= 'my-let (first exp))) :eval-my-let-fun
    (and (list? exp) (list? (first exp)) (= 'my-fn (first (first exp)))) :eval-my-fn-fun
    (list? exp) :eval-list
    :else "Error syntax..."))

;; my-eval implementation:

(defmulti my-eval dispatch-eval-type)

(defmethod my-eval :eval-symbol [symbol]
  (if-let [ns-symbol (ns-resolve 'my-eval.core symbol)]
    ns-symbol
    (if-let [let-symbol (get (:let-binding (meta symbol)) (get-keyword symbol))]
      let-symbol
      symbol)))

(defmethod my-eval :eval-keyword [keyword]
  ({:a 1 :b 2 :c 3} keyword))

(defmethod my-eval :eval-vector [vector]
  (->> vector
       (map #(merge-meta %1 (meta vector)))
       (map my-eval)))

(defmethod my-eval :eval-number-string [exp] exp)

(defmethod my-eval :eval-my-let-fun [[_ args body :as exp]]
  (let [evaluated-args (->> args
                            (map #(if (= 1 (mod (.indexOf args %1) 2)) ; To avoid passing let-binding to let symbols.
                                    (merge-meta %1 (meta exp))
                                    %1))
                            (map my-eval))
        body-with-meta (merge-meta body (meta exp))]
    (my-eval (my-let evaluated-args body-with-meta))))


(defmethod my-eval :eval-my-fn-fun [[[_ args body] & params :as exp]]
  (let [let-binding (meta exp)
        evaluated-params (->> params
                              (map #(merge-meta %1 let-binding))
                              (map my-eval))
        body-with-meta (merge-meta body let-binding)]
    (my-eval (my-fn args body-with-meta evaluated-params))))

(defmethod my-eval :eval-list [exp]
  (let [evaluated-elem (->> exp
                            (map #(merge-meta %1 (meta exp)))
                            (map my-eval))
        [func & args] evaluated-elem]
    (cond
      (and (var? func) (var? (first args))) (apply func (first args) (rest args)) ; To handler map call.
      (var? func) (apply func args)
      :else evaluated-elem)))