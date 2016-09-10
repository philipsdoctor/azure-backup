(ns azure-backup.azure-actions
  (:import (com.microsoft.azure.storage CloudStorageAccount)
           (java.io FileInputStream)))

(defn connect-to-azure-storage
  "Reads the connection string from file-name and attempts to construct a CloudStorageAccount object"
  [connection-string]
  (try
    (-> connection-string
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

(defn download-files
  [container base-folder files]
  (doseq [file files]
    (let [blob (.getBlockBlobReference container file)
          new-local-file (clojure.java.io/file base-folder file)
          local-parent (.getParentFile new-local-file)]
      (when (and (not (.exists local-parent)) (not (.mkdirs local-parent)))
        (println "Cannot create directories for file " new-local-file " halting restore")
        (System/exit 1))
      (.downloadToFile blob (.getAbsolutePath new-local-file))
      (println "Downloaded file " file))))
