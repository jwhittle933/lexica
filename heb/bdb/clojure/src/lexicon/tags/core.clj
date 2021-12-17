(ns lexicon.tags.core)

(defn tagis? [c tag] (= (:tag c) tag))

(defn- reducer [tag transform]
  (fn [children child]
    (if (and (map? child) (tagis? child tag))
      (do (conj children (transform child)))
      (do children))))

(defn accumulate [content tag transform]
  "Searches `content` for a `tag`, and applies `transform`.
  	`content` must be flattened first."
  (reduce (reducer tag transform) [] content))

