(ns bdb
  (:require
   [clojure.java.io :as io]
   [clojure.core.reducers :as r]
   [clojure.string :as strings]
   [clojure.data.json :as json]
   [tupelo.parse.xml :as txml]
   [defun.core :refer [defun]])
  (:import java.io.StringReader))

(def bdb-xml "/Users/jonathanwhittle/Development/lexica/heb/bdb/BrownDriverBriggs.xml")

(def reset "\033[0m")
(def red "\033[31m")
(def cyan "\033[36m")
(defn error [s] (str red s reset))
(defn info [s] (str cyan s reset))

(defn nofile! []
  (println (str (error "ERROR") " Filepath is required (" (info "clj -X:run :file ../path.json") ")"))
  (System/exit 1))
(defn file->name [f] (.getName (io/file f)))
(defn file->exists [f] (.exists (io/file f)))
(defn file->abspath [f] (.getAbsolutePath (io/file f)))
(defn file->read [f]
  (-> f
      (file->abspath)
      (slurp)))
(defn file->json [f] (json/read-str f :key-fn keyword))
(defn file->writejson [n json]
  (with-open [w (clojure.java.io/writer  n)]
    (json/write json w)
    (.flush w)))

;; ------------------ XML ------------------------
;; -part
;;   -section
;;     -entry
;;     -entry
;;   -section
;;     -entry
;;     -entry
;;     -entry
(defrecord Entry [id letter language type dagger mod definition])

(defn from-attrs [k unit] (k (:attrs unit)))
(defn nil-default [val def]
  (if (nil? val) def val))

(defun entry->lemma
  "Extract lemma from the entry.
	 	If the first entry is a string, skip to second and extract from :content.
	 	Some entries begin with a '[', so this must be skipped to extract the lemma."
  ([(f :guard #(string? %)), s] (first (:content s)))
  ([f _] (first (:content f)))
  ([_ _] ""))

(defun language->full
  (["heb"] "hebrew")
  (["arc"] "aramaic")
  ([_] "hebrew"))

(defun entry->tostr
  "Extracts data from an entry's content and formats it"
  ([:w _ content] (strings/trim (first content)))
  ([:pos _ content] (first content))
  ([:def _ content] (first content))
  ([:sense _ content] ;; can contain - :pos,:stem,:asp,:def,:em,:w,:ref
   (if (string? content)
     (do content)
     (do
       (strings/join " " (map
                          (fn [child]
                            (let [tag (:tag child)
                                  attrs (:attrs child)
                                  sense-content (:content child)]
                              (entry->tostr tag attrs sense-content)))
                          content)))))
  ([:stem _ content] (str  "\n" (first content)))
  ([:asp _ content] (first content))
  ([:em _ content] (first content))
  ([:ref _ content] (first content))
  ([:foreign _ content] (first content))
  ([_ _ _] ""))

(defn entry->raw [contents]
  "Extract the raw content of the entry"
  (reduce
   (fn [acc c]
     (if (string? c)
       (do (str acc (strings/trim c) " "))
       (do
         (let [tag (:tag c)
               attrs (:attrs c)
               content (:content c)]
           (str acc (entry->tostr tag attrs content) " ")))))
   ""
   contents))

(defn entry->expand [part-id section-id dict lang]
  (fn [entry]
    (let [entry-id (from-attrs :id entry)
          type (from-attrs :type entry)
          dagger (from-attrs :cite entry)
          mod (from-attrs :mod entry)
          content (:content entry)
          lemma (entry->lemma (first content) (second content))]
      {:id {:part part-id, :section section-id, :entry entry-id}
       :dict dict
       :lemma lemma
       :language (language->full lang)
       :root (if (= "root" type) true false)
       :dagger (if (= "full" dagger) true false)
       :mod (nil-default mod "I")
       :entry (strings/trim (entry->raw content))})))

(defn section->expand [part-id letter lang]
  (fn [section]
    (let [section-id (from-attrs :id section)
          entries (:content section)]
      (map (entry->expand part-id section-id letter lang) entries))))

(defn part->expand [part]
  (let [part-id (from-attrs :id part)
        letter (from-attrs :title part)
        lang (from-attrs :xml:lang part)
        sections (:content part)]
    (mapcat (section->expand part-id letter lang) sections)))

(defn lex->parse [lexicon]
  "Parse the Lexicon. mapcat until entry, collecting part and section downward"
  (let [parts (:content lexicon)]
    (mapcat part->expand parts)))

(defn run [& a]
  "Entrypoint for BDB xml"
  (let [args (remove nil? a)]
    (if (empty? args)
      (do (nofile!))
      (do
        (let [bdb (txml/parse (slurp bdb-xml))
              parsed (lex->parse bdb)
              topten (take 10 parsed)]
          (json/pprint topten :escape-unicode false)
          ; (json/pprint (nth parsed 101) :escape-unicode false)
          ; (json/pprint (nth parsed 102) :escape-unicode false)
          ; (json/pprint (nth parsed 103) :escape-unicode false)
          )))))




