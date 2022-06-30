(ns lexicon.senses.core
  (:require 
  	[lexicon.attrs.core :as attrs]
  	[lexicon.tags.core :as tags]
  	[defun.core :refer [defun]]))

(defun as-sense 
		"Senses are flat by default - no inherent association between
		 stems, asp and word forms/refs. This function needs to create associations
		 between these major breaks."
  ([sense :pos attrs content] sense)
  ([sense :stem attrs content] sense)
  ([sense :asp attrs content] sense)
  ([sense :def attrs content] sense)
  ([sense :em attrs content] sense)
  ([sense :w attrs content] sense)
  ([sense :ref attrs content] sense)
  ([sense :sense attrs content] sense)
  ([sense nil nil (content :guard #(string? %))] sense)
  ([sense _ _ _] sense))

(defun collect
  ([content] (recur [] content))
  ([children (child :guard #(and (map? %) (tags/tagis? % :sense)))]
   (conj children child))
  ([children (child :guard #(vector? %))]
   (reduce collect children child))
  ([children _] children))