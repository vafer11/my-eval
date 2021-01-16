(ns my-eval.core)

;; Aux Functions:
(defn get-keyword [symbol] (keyword (str symbol)))
(defn get-map-from-vector [args]
  (->> args
       (mapv #(if (symbol? %1)
               (get-keyword %1)
               %1))
       (apply hash-map)))

;; Implemented functions:

(defn my-plus [& inputs] (apply + inputs))

(defn my-str [& inputs] (apply str inputs))

(defn my-times [& inputs] (apply * inputs))

(defn my-inc [input] (inc input))

(defn my-map [func & inputs] (apply map func inputs))

(defn my-let
  ([args] (get-map-from-vector args))
  ([args kw-map] (merge kw-map (get-map-from-vector args))))

(defn my-fn
  ([args body params] (->> (interleave args params)
                           (get-map-from-vector)
                           (list body)))
  ([args body params kw-map] (->> (interleave args params)
                                  (get-map-from-vector)
                                  (merge kw-map)
                                  (list body))))

(defn dispatch-eval-type
  ([exp]
   (cond
     (symbol? exp) :eval-symbol
     (keyword? exp) :eval-keyword
     (vector? exp) :eval-vector
     (or (number? exp) (string? exp)) :eval-number-string
     (and (list? exp) (= 'my-let (first exp))) :eval-my-let-fun
     (and (list? exp) (= 'my-map (first exp))) :eval-my-map-fun
     (and (list? exp) (list? (first exp)) (= 'my-fn (first (first exp)))) :eval-my-fn-fun
     (list? exp) :eval-list
     :else "Error syntax..."))
  ; to implement my-let evaluation. It pass through the functions the map that handles the bindings.
  ([exp kw-map]
   (cond
     (symbol? exp) :eval-symbol
     (keyword? exp) :eval-keyword
     (vector? exp) :eval-vector
     (or (number? exp) (string? exp)) :eval-number-string
     (and (list? exp) (= 'my-let (first exp))) :eval-my-let-fun
     (and (list? exp) (= 'my-map (first exp))) :eval-my-map-fun
     (and (list? exp) (list? (first exp)) (= 'my-fn (first (first exp)))) :eval-my-fn-fun
     (list? exp) :eval-list
     :else "Error syntax my-let evaluation path...")))

;; my-eval implementation:

(defmulti my-eval dispatch-eval-type)

(defmethod my-eval :eval-symbol
  ([symbol]
   (if-let [resolved-symbol (ns-resolve 'my-eval.core symbol)]
     resolved-symbol
     symbol))
  ([symbol kw-map]
   ; Return value from map, for those symbols that are not functions
   (if-let [resolved-symbol (ns-resolve 'my-eval.core symbol)]
     resolved-symbol
     (kw-map (get-keyword symbol)))))

(defmethod my-eval :eval-keyword
  ([exp] ({:a 1 :b 2 :c 3} exp))
  ([exp opt-map] ({:a 1 :b 2 :c 3} exp)))

(defmethod my-eval :eval-vector
  ([exp] (map my-eval exp))
  ([exp opt-map] (map #(my-eval %1 opt-map) exp)))

(defmethod my-eval :eval-number-string
  ([exp] exp)
  ([exp opt-map] exp))

(defmethod my-eval :eval-my-let-fun
  ([[_  args body]]
   (let [kw-map (my-let (mapv my-eval args))]
     (my-eval body kw-map)))
  ([[_ args body] kw-map]
   ; to implement evaluation for nested my-let
   (let [kw-map (my-let (mapv my-eval args) kw-map)]
     (my-eval body kw-map))))

(defmethod my-eval :eval-my-map-fun
  ([exp]
  (let [[my-map-fun map-fun & map-args] (map my-eval exp)]
    (apply my-map-fun map-fun map-args)))
  ([exp opt-map]
   (let [[my-map-fun map-fun & map-args] (map #(my-eval %1 opt-map) exp)]
     (apply my-map-fun map-fun map-args))))

(defmethod my-eval :eval-my-fn-fun
  ([[[_ args body] & params]]
   (let [evaluated-params (map my-eval params)
         [fn-body fn-kw-map] (my-fn args body evaluated-params)]
     (my-eval fn-body fn-kw-map)))
  ([[[_ args body] & params] kw-map]
   (let [evaluated-params (map my-eval params)
         [fn-body fn-kw-map] (my-fn args body evaluated-params kw-map)]
     (my-eval fn-body fn-kw-map))))

(defmethod my-eval :eval-list
  ([exp]
   (let [evaluated-elem (map my-eval exp) [func & args] evaluated-elem]
     (cond
       (var? func) (apply func args)
       :else evaluated-elem)))
  ([exp kw-map]
   (let [[func & args] (map #(my-eval %1 kw-map) exp)]
     (apply func args))))