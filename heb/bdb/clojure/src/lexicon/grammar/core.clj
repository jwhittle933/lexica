(ns lexicon.grammar.core
  (:require [clojure.string :as strings]
            [defun.core :refer [defun]]))

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
                      :abstr "abstract"
                      :adj "adjective"
                      :gent "gentilic"
                      :interj "interjection"
                      :interr "interrogative"
                      :interrog "interrogative"
                      :conj "conjuction"
                      :demonstr "demonstrative"
                      :prep "preposition"
                      :unit "unit"
                      :num "number"
                      :ordin "ordinal"
                      :denom "denominative"
                      :subst "substantive"
                      :terr "terrestrial"
                      :pers "personal"
                      :emph "emphatic"
                      :quadril "quadriliteral"
                      :intensive "intensive"
                      :intens "intensive"
                      :appell "appellative"
                      (keyword "enclitic part") "enclictic particle"
                      (keyword "particle of negation") "particle of negation"
                      :epith "epithet"})

(defn pos->reducer [acc s]
  (conj
   acc
   ((keyword (strings/trim s)) parts-of-speech)))

(defn parse->pos [pos]
  "`pos` will be a . separated string.
			The order of the parts will determine the meaning
			of the abbreivation in some cases."
  (-> pos
      (strings/replace #"(\[|\])" "")
      (strings/split #"\.")
      (#(reduce pos->reducer [] %))))
