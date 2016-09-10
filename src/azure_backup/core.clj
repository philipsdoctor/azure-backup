(ns azure-backup.core
  (:require [azure-backup.server-files :as sf]
            [azure-backup.command :as command]
            [azure-backup.azure-actions :as actions]
            [clojure.set :as cset])
  (:gen-class))

(defn- local-folder->relative-paths
  [base-folder]
  (let [file-base (.toURI base-folder)]
    (->> (file-seq base-folder)
         (filter #(not (.isDirectory %)))
         (map #(->> (.toURI %) (.relativize file-base) (.getPath))))))

(defn paths->filtered-set
  [prefix paths]
  (->> paths
       (filter #(.startsWith % prefix))
       (into #{})))

(defn backup
  [container server-paths base-folder local-paths]
  (let [new-server-paths (actions/upload-files container local-paths server-paths base-folder)]
    (println "Persisting cache of files to local-file-cache.")
    (spit "local-file-cache" new-server-paths)
    (println "Finished backup!")))

(defn restore
  [container server-paths base-folder local-paths prefix]
  (let [requested-server-restore (paths->filtered-set prefix server-paths)
        local-existing (paths->filtered-set prefix local-paths)
        missing-files (cset/difference requested-server-restore local-existing)]
    (actions/download-files container base-folder missing-files))
  (println "Finished restore!"))

(defn -main
  [& args]
  (let [[job-type params] (command/get-job-type args)
        {:keys [base-folder connection-string container]} (clojure.edn/read-string (slurp "settings.clj"))
        base-folder (clojure.java.io/file base-folder)
        ;; validation would be nice
        service-client (actions/connect-to-azure-storage connection-string)
        container (actions/get-or-create-container service-client container)
        server-paths (sf/get-paths-from-local-or-server container "local-file-cache")
        local-paths (local-folder->relative-paths base-folder)]
    (case job-type
      :restore (restore container server-paths base-folder local-paths params)
      :backup (backup container server-paths base-folder local-paths))))
