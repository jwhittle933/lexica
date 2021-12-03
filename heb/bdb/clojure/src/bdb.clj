(ns bdb 
  (:require 
    [clojure.java.io :as io]
    [clojure.data.json :as json]))

(defn file->name [f] (.getName (io/file f)))
(defn file->exists [f] (.exists (io/file f)))
(defn file->abspath [f] (.getAbsolutePath (io/file f)))
(defn file->read [f] 
  (-> f
      (file->abspath)
      (slurp)))
(defn file->json [f] (json/read-str f))

(def reset "\033[0m")
(def red "\033[31m")
(def cyan "\033[36m")
(defn error [s] (str red s reset))
(defn info [s] (str cyan s reset))

(defn run [& a]
  (let [args (remove nil? a)]
    (if (empty? args)
      (do
        (println (str (error "ERROR") " Filepath is required (" (info "clj -X:run :file ../path.json") ")"))
        (System/exit 1))
      (do
        (println (str "Current Working Directory: " (info (System/getProperty "user.dir"))))
        (let [file (str (System/getProperty "user.dir") "/" (:file (first args)))]
          (println (str "File: " (info file)))
          ;; Read json file
          (println (file->json (file->read file))))))))
