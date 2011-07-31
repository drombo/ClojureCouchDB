(ns ClojureCouchDB.core
  (:import (org.jcouchdb.db Database)))

(def db (Database. ("localhost", "sbrdb")))
(type db)

;;(def doc 
