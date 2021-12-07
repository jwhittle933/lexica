(ns bdb
  (:require
   [clojure.java.io :as io]
   [clojure.data.json :as json]
   [tupelo.parse.xml :as txml]
   [defun.core :refer-macros  [defun]])
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

(defn print->currentdir []
  (println (str "Current Working Directory: " (info (System/getProperty "user.dir")))))

(defn print->file [f]
  (println (str "File: " (info f) "\n")))

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
(defrecord Entry [meta letter language type allRefs mod])

(defn from-attrs [k unit] (k (:attrs unit)))
(defn nil-default [val def]
	(if (nil? val) def val))

(defn lex->parse [lexicon]	
  (let [parts (:content lexicon)]
  	(mapcat 
  		(fn [part] 
  			(let [part-id (from-attrs :id part)
          	letter (from-attrs :title part)
         		lang (from-attrs :xml:lang part)
         	 sections (:content part)]
  				(mapcat 
  					(fn [section] 
  						(let [section-id (from-attrs :id section)
  												entries (:content section)] 
  												(map
  													(fn [entry] 
  														(let [entry-id (from-attrs :id entry)
  																				type (from-attrs :type entry)
  																				allRefs (from-attrs :cite entry)
  																				mod (from-attrs :mod entry)]
  															(->Entry 
  																{:partID part-id, :sectionID section-id, :entryID entry-id} 
  																letter 
  																lang 
  																type 
  																(nil-default allRefs false)
  																(nil-default mod "I"))))
  													entries)))
  					sections)))
  		parts)))

(defn xml [& a]
  (let [args (remove nil? a)]
    (if (empty? args)
      (do (nofile!))
      (do
        (let [bdb (txml/parse (slurp bdb-xml))]
          (println (first (lex->parse bdb))))))))




