(ns ClojureCouchDB.core
  (:import (org.jcouchdb.db Database))
  (:require clojure.contrib.logging))

(def db (Database. "localhost" "sbrdb"))

; prints all files
(import 'java.io.File)
(defn walk [dirpath]
  (doseq [file (file-seq (File. dirpath))]

    (println (.getPath file))

    (.createDocument db
      (doto (java.util.HashMap.)
        (.put "name" (.getName file))
        (.put "size" (.length file))
        (.put "path" (.getPath file))
        )
      )
    )
  )

(walk "/Users/sbr/Tools")

