(ns bdb
  (:require
   [lexicon.core :as lexicon]
   [clojure.data.json :as json]
   [tupelo.parse.xml :as xml])
  (:import java.io.StringReader))

(def bdb-xml "/Users/jonathanwhittle/Development/lexica/heb/bdb/BrownDriverBriggs.xml")

;; ------------------ BDB XML ------------------------
(defn run [& a]
  "Entrypoint for BDB xml"
  (let [bdb (xml/parse (slurp bdb-xml))
        parsed (lexicon/parse bdb)
        topten (take 20 parsed)]
    (json/pprint (lexicon/apply-pages topten) :escape-unicode false)))

