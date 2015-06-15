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

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (about-page))
  (GET "/:register/hash/:hash" [register hash]
       (let [json-url (str "http://" register ".openregister.org/hash/" hash ".json")
             record   (json/parse-string (slurp json-url) true)]
         (entry-page {:name register :copyright "copyright bar"}
                     (assoc record :primary (get-in record [:entry (keyword register)]))
                     [])))
  (GET "/:register/:value" [register value]
       (let [json-url (str "http://" register ".openregister.org/" register "/" value ".json")
             record   (json/parse-string (slurp json-url) true)]
         (entry-page {:name register :copyright "copyright foo"}
                     (assoc record :primary (get-in record [:entry (keyword register)]))
                     []))))

