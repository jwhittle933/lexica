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
(defn tagis? [c tag] (= (:tag c) tag))
(defn from-attrs [k unit] (k (:attrs unit)))
(defn nil-default [val def]
  (if (nil? val) def val))

(defn accumulate-tags-with [content tag func]
  "Searches content for a `tag`, and applies `func`"
  (reduce
   (fn [children child]
     (if (and (map? child) (tagis? child tag))
       (do (conj children (func child)))
       (do children)))
   []
   content))

(defn ref->fromsbl [ref]
		(reduce #(strings/replace-first %1 #"\." %2) ref [" " ":"]))

(defun content->flatten
  "Flatten an entry's content.
		Content can be a string, a vector of children, or a nested vector of entries"
  ([content] (recur [] content))
  ([children (child :guard #(string? %))]
   (conj children (strings/trim child)))
  ([children (child :guard #(vector? %))]
   (reduce content->flatten children (flatten child)))
  ([children (child :guard #(and (map %) (> (count (:content %)) 1)))]
   (recur children (:content child)))
  ([children (child :guard #(map? %))]
   (conj children child))
  ([children child] (recur children (:content child)))
  ([children _] children))

(defun entry->lemma
  "Extract lemma from the entry.
	 	If the first entry is a string, skip to second and extract from :content.
	 	Some entries begin with a '[', so this must be skipped to extract the lemma."
  ([(f :guard #(string? %)) s] (first (:content s)))
  ([f _] (first (:content f))))

(defun language->full
  (["heb"] "hebrew")
  (["arc"] "aramaic")
  ([_] "hebrew"))

(defun entry->tostr
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
                (str senses " " (entry->tostr tag attrs sense-content))))))
        ""
        content))))
  ([:stem _ content] (str  "\n" (first content)))
  ([:asp _ content] (first content))
  ([:em _ content] (first content))
  ([:ref _ content] (first content))
  ([:foreign _ content] (first content)) ;; parse out greek, latin, etc
  ([_ _ _] ""))

(defn entry->raw [full? contents]
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
   (if full? "† " "")
   contents))

(defn apply->ids [out part section entry]
  (assoc out :_id {:part part :section section entry entry}))

(defn apply->references [out flat]
  (assoc
   out
   :references
   (accumulate-tags-with flat :ref #(ref->fromsbl (from-attrs :r %)))))

(defn apply->page [out flat]
  (let [p (first (accumulate-tags-with flat :page #(from-attrs :p %)))]
    (if (nil? p) out (do (assoc out :page p)))))

(defn apply->words [out flat]
  (assoc out :words (accumulate-tags-with flat :w #(first (:content %)))))

(defn apply->definitions [out flat]
  (assoc out :definitions (accumulate-tags-with flat :def #(first (:content %)))))

(defn apply->pos [out flat root?]
  (assoc out :pos (if (not root?)
                    (do (first (accumulate-tags-with flat :pos #(first (:content %)))))
                    "root")))

(defn apply->stems [out flat]
  (let [s (accumulate-tags-with flat :stem #(first (:content %)))]
    (if (= (count s) 0) out (assoc out :stems s))))

(defn apply->entry [out content full?]
  (assoc out :entry (strings/trim (entry->raw full? content))))

(defn apply-all-pages 
		"Retroactively applies pages to unmarked items.
		 Where an entry has no page, it's `staged`. When a page ref is
		 found, that page is applied to all `staged` and then `committed`."
		([entries] (apply-all-pages entries [] []))
		([entries committed staged] entries))

(defn entry->expand [part-id section-id dict lang]
  (fn [entry]
    (let [entry-id (from-attrs :id entry)
          root? (if (= "root" (from-attrs :type entry)) true false)
          full? (if (= "full" (from-attrs :cite entry)) true false)
          mod (nil-default (from-attrs :mod entry) "I")
          content (:content entry)
          lemma (entry->lemma (first content) (second content))
          flat (content->flatten content)]
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
          (apply->pos flat root?)
          (apply->stems flat)
          (apply->entry content full?)))))

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
          (json/pprint topten :escape-unicode false))))))

