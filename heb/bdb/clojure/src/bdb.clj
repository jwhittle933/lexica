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
  ; (->> bdb-xml
  ; 	(slurp)
  ; 	(txml/parse)
  ; 	(take 10)
  ; 	#(json/pprint % :escape-unicode false))
  (let [bdb (xml/parse (slurp bdb-xml))
        parsed (lexicon/parse bdb)
        topten (take 10 parsed)]
    (json/pprint topten :escape-unicode false)))

