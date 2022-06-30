(ns lexicon.content.core
  (:require
   [clojure.string :as strings]
   [defun.core :refer [defun]]))

(defun to-flat
  "Flatten an entry's content.
		Content can be a string, a vector of children, or a nested vector of entries"
  ([content] (recur [] content))
  ([children (child :guard #(string? %))]
   (conj children (strings/trim child)))
  ([children (child :guard #(vector? %))]
   (reduce to-flat children (flatten child)))
  ([children (child :guard #(and (map %) (> (count (:content %)) 1)))]
   (recur children (:content child)))
  ([children (child :guard #(map? %))]
   (conj children child))
  ([children child] (recur children (:content child)))
  ([children _] children))

