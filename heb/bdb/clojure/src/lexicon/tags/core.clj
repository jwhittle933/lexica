(ns lexicon.tags.core)

(defn tagis? [c tag] (= (:tag c) tag))

(defn accumulate [content tag func]
  "Searches `content` for a `tag`, and applies `func`.
  	`content` must be flattened first."
  (reduce
   (fn [children child]
     (if (and (map? child) (tagis? child tag))
       (do (conj children (func child)))
       (do children)))
   []
   content))

