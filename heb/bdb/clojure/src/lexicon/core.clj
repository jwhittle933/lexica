(ns lexicon.core
  (:require [lexicon.entry.core :as entry]
            [lexicon.attrs.core :as attrs]))

(defn apply-pages
		"Applies page number to each entry. Current page starts at 0, and increments when a 
		 page is encountered on an entry. The first entry is marked, so 0 is incremented on first
		 iter."
		([entries] (apply-pages entries [] [] 0))
  ([entries committed staged current-page]
   (if (empty? entries)
     committed
     (do
       (let [e (first entries)
             page (:page e)]
         (if (nil? page)
           (do (recur (rest entries) committed (conj staged (assoc e :page current-page)) current-page))
           (do
             (recur
              (rest entries)
              (concat committed (map #(assoc % :page (+ 1 current-page)) staged))
              []
              (+ 1 current-page)))))))))

(defn section->expand [part-id letter lang]
  (fn [section]
    (let [section-id (attrs/from-attrs :id section)
          entries (:content section)]
      (->> entries
           (map (entry/expand part-id section-id letter lang))))))

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
