(ns lexicon.core
  (:require [lexicon.entry.core :as entry]
            [lexicon.attrs.core :as attrs]))

(defn section->expand [part-id letter lang]
  (fn [section]
    (let [section-id (attrs/from-attrs :id section)
          entries (:content section)]
      (map (entry/expand part-id section-id letter lang) entries))))

(defn part->expand [part]
  (let [part-id (attrs/from-attrs :id part)
        letter (attrs/from-attrs :title part)
        lang (attrs/from-attrs :xml:lang part)
        sections (:content part)]
    (mapcat (section->expand part-id letter lang) sections)))

(defn parse [lexicon]
  "Parse the Lexicon. mapcat until entry, collecting part and section downward"
  (let [parts (:content lexicon)]
    (mapcat part->expand parts)))
