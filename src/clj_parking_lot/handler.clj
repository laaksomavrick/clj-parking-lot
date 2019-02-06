(ns clj-parking-lot.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))

(s/defschema Ticket
  {:ticket {:id s/Int}})

(defonce tickets (atom (hash-map)))

(defn create-ticket! []
  (let [id (inc (count @tickets))
        ticket {:id id}]
    (swap! tickets assoc id ticket)
    ticket))

(def app
  (api
   {:swagger
    {:ui "/"
     :spec "/swagger.json"
     :data {:info {:title "clj-parking-lot"
                   :description "Compojure Api example"}
            :tags [{:name "api", :description "some apis"}]}}}

   (POST "/customers" []
     :return Ticket
     :summary "Issues a new ticket for the requesting client"
     (ok {:ticket (create-ticket!)}))))
