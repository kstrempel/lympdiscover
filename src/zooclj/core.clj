(ns zooclj.core
  (:gen-class)
  (:require [zookeeper :as zk]
            [zookeeper.data :as data]
            [zookeeper.util :as util]
            [clojure.data.json :as json]))

(def client (zk/connect "127.0.0.1:2181"))

(def main-path "/lymph/services")

(defn lymph-services []
  (zk/children client main-path))

(defn children-ips [service children]
  (clojure.string/join
   ", "
   (map #(let [data (json/read-str
                     (data/to-string (:data (zk/data client (str main-path "/" service "/" %1))))
                     :key-fn keyword)]
           (:endpoint data))
        children)))

(defn -main []
  (loop [services (lymph-services)]
    (if-not (empty? services)
      (let [service (first services)
            children (zk/children client (str main-path "/" service))]
        (println service (str (children-ips service children)))
        (recur (rest services))))))
