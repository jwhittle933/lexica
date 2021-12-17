(ns lexicon.entry.core
  (:require [lexicon.grammar.core :as grammar]
            [lexicon.attrs.core :as attrs]
            [lexicon.content.core :as content]
            [lexicon.tags.core :as tags]
            [clojure.string :as strings]
            [defun.core :refer [defun]]))

(defn nil-default [val def]
  (if (nil? val) def val))

(defn first-content [x] (first (:content x)))

(defn ref->fromsbl [ref]
  (reduce #(strings/replace-first %1 #"\." %2) ref [" " ":"]))

(defun language->full
  (["heb"] "hebrew")
  (["arc"] "aramaic")
  ([_] "hebrew"))

(defun tostr
  "Extracts data from an entry's content and formats it. To be used as reducer"
  ([:w _ content] (strings/trim (first content)))
  ([:pos _ content] (first content))
  ([:def _ content] (first content))
  ([:sense _ content] ;; can contain - :pos,:sense,:stem,:asp,:def,:em,:w,:ref
   (if (string? content)
     (do content)
     (do
       (reduce
        (fn [senses child]
          (if (string? child)
            (do (str senses " " (strings/trim child)))
            (do
              (let [tag (:tag child)
                    attrs (:attrs child)
                    sense-content (:content child)]
                (str senses " " (tostr tag attrs sense-content))))))
        ""
        content))))
  ([:stem _ content] (str  "\n" (first content)))
  ([:asp _ content] (first content))
  ([:em _ content] (first content))
  ([:ref _ content] (first content))
  ([:foreign _ content] (first content)) ;; parse out greek, latin, etc
  ([_ _ _] ""))

(defn raw [full? contents]
  "Extract the raw content of the entry"
  (reduce
   (fn [acc c]
     (if (string? c)
       (do (str acc (strings/trim c) " "))
       (do
         (let [tag (:tag c)
               attrs (:attrs c)
               content (:content c)]
           (str acc (tostr tag attrs content) " ")))))
   (if full? "† " "")
   contents))

(defn apply->ids [out part section entry]
  (assoc out :_id {:part part :section section entry entry}))

(defn apply->references [out flat]
  (assoc
   out
   :references
   (tags/accumulate flat :ref #(ref->fromsbl (attrs/from-attrs :r %)))))

(defn apply->page [out flat]
  (let [p (first (tags/accumulate flat :page #(attrs/from-attrs :p %)))]
    (if (nil? p) out (do (assoc out :page p)))))

(defn apply->words [out flat]
  "Accumulates words by tag."
  (assoc out :words (tags/accumulate flat :w #(first (:content %)))))

(defn apply->definitions [out flat]
  (assoc out :definitions (tags/accumulate flat :def first-content)))

(defn apply->grammar [out flat root?]
  (let [grammar (first (tags/accumulate flat :pos first-content))]
    (if (nil? grammar)
      out
      (do
        (assoc out :grammar (grammar/parse->pos grammar))))))

(defn apply->stems [out flat]
  (let [s (tags/accumulate flat :stem first-content)]
    (if (= (count s) 0) out (assoc out :stems s))))

(defn apply->entry [out content full?]
  (assoc out :entry (strings/trim (raw full? content))))

(defun lemma
  "Extract lemma from the entry.
	 	If the first entry is a string, skip to second and extract from :content.
	 	Some entries begin with a '[', so this must be skipped to extract the lemma."
  ([(f :guard #(string? %)) s] (first-content s))
  ([f _] (first (:content f))))

(defn expand [part-id section-id dict lang]
  (fn [entry]
    (let [entry-id (attrs/from-attrs :id entry)
          root? (if (= "root" (attrs/from-attrs :type entry)) true false)
          full? (if (= "full" (attrs/from-attrs :cite entry)) true false)
          mod (nil-default (attrs/from-attrs :mod entry) "I")
          content (:content entry)
          lemma (lemma (first content) (second content))
          flat (content/to-flat content)]
      (-> {}
          (apply->ids part-id section-id entry-id)
          (assoc :dict dict)
          (assoc :lemma lemma)
          (assoc :language (language->full lang))
          (assoc :completeCitation full?)
          (assoc :mod mod)
          (apply->references flat)
          (apply->page flat)
          (apply->words flat)
          (apply->definitions flat)
          (apply->grammar flat root?)
          (apply->stems flat)
          (apply->entry content full?)))))
