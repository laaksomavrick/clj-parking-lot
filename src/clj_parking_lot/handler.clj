(ns clj-parking-lot.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [compojure.api.exception :as ex]))

(s/defschema Ticket
  {:ticket {:id s/Int :paid s/Bool :owing s/Int :rate s/Int}})

(s/defschema TicketInput
  {:rate s/Int})

(def parking-lot-capacity 24)
(def tickets (atom (hash-map)))
(def rates {0 300  ;; 1 hr
            1 450  ;; 3 hr
            2 600  ;; 6 hr
            3 750  ;; all day
            })

(defn below-capacity?
  []
  (< (count @tickets) parking-lot-capacity))

;; todo handle invalid rate
(defn get-owing
  [rate]
  (get rates rate))

(defn create-ticket!
  [ticket-input]
  (let [id (inc (count @tickets))
        owing (get-owing (get ticket-input :rate))
        ticket (merge ticket-input {:id id :paid false :owing owing})]
    (swap! tickets assoc id ticket)
    ticket))

(def app
  (api

   (POST "/customers" []
     :body [ticket-input TicketInput]
     :return Ticket
     ;; use cond to check that rate is valid here
     (if (below-capacity?)
       (ok {:ticket (create-ticket! ticket-input)})
       (bad-request {:error "The parking lot is full!"})))

   (GET "/tickets/:id" []
     :return Ticket
     :path-params [id :- Long]
     (if-let [ticket (@tickets id)]
       (ok {:ticket ticket})
       (not-found {:error "Ticket not found"})))

   (GET "/tickets" []
     (ok {:tickets @tickets}))))
