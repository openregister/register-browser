(ns register-browser.routes.home
  (:require [register-browser.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :refer [ok]]
            [clojure.java.io :as io]))

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
       (entry-page {:name register :copyright "copyright bar"}
                   {:name "Cabinet office"
                    :fields ["name" "value"]
                    :last-updated (org.joda.time.DateTime.)
                    :latest-version-url (str "/" register "/" "fakevalue")
                    :hash hash}
                   []))
  (GET "/:register/:value" [register value]
       (entry-page {:name register :copyright "copyright foo"}
                   {:name "Cabinet office"
                    :fields ["name" "value"]
                    :last-updated (org.joda.time.DateTime.)
                    :latest-version-url (str "/" register "/" value)
                    :hash "abcdef123456"}
                   [])))

