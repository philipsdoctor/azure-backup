(ns azure-backup.core
  (:require [azure-backup.server-files :as sf])
  (:import (com.microsoft.azure.storage CloudStorageAccount)
           (java.io FileInputStream))
  (:gen-class))

(defn local-folder->relative-paths
  [base-folder]
  (let [file-base (.toURI base-folder)]
    (->> (file-seq base-folder)
         (filter #(not (.isDirectory %)))
         (map #(->> (.toURI %) (.relativize file-base) (.getPath))))))

(defn connect-to-azure-storage
  "Reads the connection string from file-name and attempts to construct a CloudStorageAccount object"
  [file-name]
  (try
    (-> (slurp file-name)
        (clojure.string/trim-newline)
        (clojure.string/trim)
        (CloudStorageAccount/parse)
        (.createCloudBlobClient))
    (catch java.security.InvalidKeyException e
      (println (.getMessage e))
      (println "This error most likely indicates that you either misentered your credentials.")
      (System/exit 1))))

(defn get-or-create-container
  "Gets the storage container"
  [service-client container-name]
  (let [container (.getContainerReference service-client container-name)]
    (.createIfNotExists container)
    container))

(defn upload-files
  [container local-paths server-paths base-folder]
  (loop [[local-path & rest-paths] local-paths
         new-server-paths server-paths]
    (if local-path
      (if (get server-paths (.toString local-path))
        (recur rest-paths new-server-paths)
        (let [blob (.getBlockBlobReference container (.toString local-path))
              local-file (clojure.java.io/file base-folder local-path)]
          (println "Uploading new file " (.toString local-path))
          (.upload blob (FileInputStream. local-file) (.length local-file))
          (recur rest-paths (conj new-server-paths local-path))))
      new-server-paths)))

(defn -main
  [& args]
  (let [settings (clojure.edn/read-string (slurp "settings.clj"))
        base-folder (clojure.java.io/file (:base-folder settings))
        ;; validation would be nice
        service-client (connect-to-azure-storage (:connection-string settings))
        container (get-or-create-container service-client "testcontainer")
        server-paths (sf/get-paths-from-local-or-server container "local-file-cache")
        local-paths (local-folder->relative-paths base-folder)
        new-server-paths (upload-files container local-paths server-paths base-folder)]
    (println "Persisting cache of files to local-file-cache.")
    (spit "local-file-cache" new-server-paths)
    (println "Done")))
