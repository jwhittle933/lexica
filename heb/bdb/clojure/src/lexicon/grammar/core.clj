(ns lexicon.grammar.core
  (:require [clojure.string :as strings]))

(def parts-of-speech {:n "noun"
                      :vb "verb"
                      :pron "pronoun"
                      :pr "proper"
                      :part "particle"
                      :pt "participle"
                      :coll "collective"
                      :m "masculine"
                      :f "feminine"
                      :du "dual"
                      :comm "common"
                      :indecl "indeclinable"
                      :adj "adjective"
                      :gent "gentilic"
                      :interj "interjection"
                      :interr "interrogative"
                      :interrog "interrogative"
                      :demonstr "demonstrative"
                      :prep "preposition"
                      :unit "unit"
                      :num "number"
                      :ordin "ordinal"
                      :denom "denominative"
                      :subst "substantive"
                      :terr "terra"
                      :abstr "abstract"
                      :pers "personal"
                      :emph "emphatic"
                      :quadril "quadriliteral"
                      :intensive "intensive"
                      :epith "epithet"})

(defn parse->pos [pos]
  (-> pos
      (strings/split #"\.")))
