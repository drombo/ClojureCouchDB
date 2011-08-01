(ns ClojureCouchDB.core
  (:import (org.jcouchdb.db Database))
  (:require clojure.contrib.logging))

(def db (Database. "localhost" "sbrdb"))

(def adoc (doto (java.util.HashMap.) (.put "k1" "v1") (.put "k2" "v2")))

(.createDocument db adoc)

