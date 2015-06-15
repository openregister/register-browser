(ns register-browser.routes.home
  (:require [register-browser.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :refer [ok]]
            [clojure.java.io :as io]
            [cheshire.core :as json]
            [markdown.core :refer [md-to-html-string]])
  (:import [java.net URLEncoder]))


(defn entry-page [register record history]
  (layout/render
   "entry.html" {:register register
                 :record   record
                 :history  [{:hash "1234" :timestamp (org.joda.time.DateTime.)}]
                 :representations [{:name "ttl" :url "foo.ttl"}]}))

(defn render-curie [register value]
  (str "http://" register ".openregister.org/" (URLEncoder/encode register) "/" (URLEncoder/encode value) ".json"))

(defn render-as [field value]
  (condp = (:datatype field)
    "text" (md-to-html-string value)
    value))

(defn render-entry [entry]
  (into {}
        (for [[field-keyword value] entry
              :let [field-name (name field-keyword)]
              :let [field (:entry (json/parse-stream (clojure.java.io/reader (render-curie "field" field-name)) true))]]
          [field-name (render-as field value)])))

(defn fetch-curie [register value]
  (json/parse-stream (clojure.java.io/reader (render-curie register value)) true))

(defroutes home-routes

  (GET "/:register/hash/:hash" [register hash]
       (let [json-url (str "http://" register ".openregister.org/hash/" hash ".json")
             record   (json/parse-string (slurp json-url) true)]
         (entry-page (:entry (fetch-curie "register" register))
                     (assoc record
                       :primary (get-in record [:entry (keyword register)])
                       :rendered (render-entry (:entry record)))
                     [])))
  (GET "/:register/:value" [register value]
       (let [json-url (render-curie register value)
             record   (json/parse-string (slurp json-url) true)]
         (entry-page (:entry (fetch-curie "register" register))
                     (assoc record
                       :primary (get-in record [:entry (keyword register)])
                       :rendered (render-entry (:entry record)))
                     []))))

