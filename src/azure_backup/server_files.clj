(ns azure-backup.server-files
  (:import (com.microsoft.azure.storage.blob CloudBlobDirectory)
           (java.io FileNotFoundException)))

(defn- all-blobs
  "Lists all blobs in a container, walks blob directories, uses real recursion so beware."
  [container]
  (let [blobs (.listBlobs container)]
    (for [blob blobs]
      (if (instance? CloudBlobDirectory blob)
          (all-blobs blob)
          blob))))

(defn- all-relative-paths
  "Converts blobs to relative paths for comparison with local file paths"
  [container blobs]
  (let [container-base (.getUri container)]
    (map #(.getPath (.relativize container-base %)) blobs)))

(defn container->server-paths
  "Given a container, will find all blob URIs relative to the container and build a set.  Incurs usage fees."
  [container]
  (println "Missing local cache, rebuilding cache from server.")
  (->> (all-blobs container)
       (flatten)
       (map #(.getUri %))
       (all-relative-paths container)
       (into #{})))

(defn- read-clj-data-from-local
  [local-path]
  (try
    (clojure.edn/read-string (slurp local-path))
    (catch FileNotFoundException _
      #{})))

(defn get-paths-from-local-or-server
  "Given a container and a local path, will try to get a cache of server paths, if empty, it will rebuild the local cache"
  [container local-path]
  (let [local-file-cache (read-clj-data-from-local local-path)]
    (if (empty? local-file-cache)
      (container->server-paths container)
      local-file-cache)))
