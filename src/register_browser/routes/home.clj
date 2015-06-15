(ns register-browser.routes.home
  (:require [register-browser.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :refer [ok]]
            [clojure.java.io :as io]
            [cheshire.core :as json]))

(defn home-page []
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page []
  (layout/render "about.html"))

(defn entry-page [register record history]
  (layout/render
   "entry.html" {:register register
                 :record   record
                 :history  [{:hash "1234" :timestamp (org.joda.time.DateTime.)}]
                 :representations [{:name "ttl" :url "foo.ttl"}]}))

(defn render-curie [register value]
  (str "http://" register ".openregister.org/" register "/" value ".json"))

(defn render-entry [entry]
  (into {}
        (for [[field-keyword value] entry
              :let [field-name (name field-keyword)]
              :let [field (json/parse-stream (clojure.java.io/reader (render-curie "field" field-name)))]
              :let [datatype (:datatype field)]]
          [field-name value])))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (about-page))
  (GET "/:register/hash/:hash" [register hash]
       (let [json-url (str "http://" register ".openregister.org/hash/" hash ".json")
             record   (json/parse-string (slurp json-url) true)]
         (entry-page {:name register :copyright "copyright bar"}
                     (assoc record
                       :primary (get-in record [:entry (keyword register)])
                       :rendered (render-entry (:entry record)))
                     [])))
  (GET "/:register/:value" [register value]
       (let [json-url (render-curie register value)
             record   (json/parse-string (slurp json-url) true)]
         (entry-page {:name register :copyright "copyright foo"}
                     (assoc record
                       :primary (get-in record [:entry (keyword register)])
                       :rendered (render-entry (:entry record)))
                     []))))

