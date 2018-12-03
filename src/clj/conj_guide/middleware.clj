(ns conj-guide.middleware
  (:require
   [cheshire.core :refer :all]
   [clojure.java.io :as io]
   [clojure.edn :as edn]
   [clojure.string :as str]))

(defn ingest-geojson-data
  "Read in a GeoJSON File into a Clojure Data Structure"
  [geojson-resource]
  (parse-string
   (slurp
    (io/resource (str "data/polygons/" geojson-resource))) true))

(defn middleware
  []
  (let [payload {}]
    (assoc-in payload [:borders :dt] (ingest-geojson-data "dt-durham.geo.json"))))

