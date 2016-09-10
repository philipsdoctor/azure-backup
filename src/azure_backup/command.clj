(ns azure-backup.command
  (:require [clojure.tools.cli :refer [parse-opts]]))

(def cli-options
  [["-r" "--restore PREFIX" "Restore all files including folders and sub-folders that start with 'prefix', does not replace local files of the same name."
    :default nil
    :id :restore]
   ["-b" nil "Backup folder specified in settings.clj, mutually exclusive with restore action, this is the default action."
    :default nil
    :id :backup]
   ["-h" "--help"]])

(defn- validate-options
  [options]
  (when (and (get-in options [:options :restore]) (get-in options [:options :backup]))
    (println "Either backup or restore may be chosen, but not both.")
    (System/exit 1))
  options)

(defn- options->action
  [options]
  (if-let [to-restore (get-in options [:options :restore])]
    [:restore to-restore]
    [:backup nil]))

(defn get-job-type
  [args]
  (-> (parse-opts args cli-options)
      (validate-options)
      (options->action)))
