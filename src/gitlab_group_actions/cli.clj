(ns gitlab-group-actions.cli
  (:use [docopt.core :only [docopt]])
  (:require [gitlab-group-actions.excludes :as excludes]))

(defn- normalize [arg-map] {:access-token (arg-map "<access-token>")
                            :action       (cond (arg-map "start-pipeline") :start-pipeline
                                                (arg-map "create-tag") :create-tag :else :exit)
                            :api-url      (arg-map "<api-url>")
                            :branch       (cond (= (arg-map "--branch") ":default") :default
                                                :else (arg-map "--branch"))
                            :dry-run      (or (arg-map "--dry-run") false)
                            :excludes     (if (arg-map "--excludes-file") (excludes/from-file (arg-map "--excludes-file")))
                            :includes     (if (arg-map "--includes-file") (excludes/from-file (arg-map "--includes-file")))
                            :exit-status  (arg-map "exit-status")
                            :group-id     (if (arg-map "<group-id>") (Integer/parseInt (arg-map "<group-id>")))
                            :recursive    (or (arg-map "--recursive") false)
                            :topic        (when-let [t (arg-map "--topic")] (.replaceFirst t "^=" ""))
                            :tag-name     (arg-map "<name>")
                            :tag-message  (arg-map "<message>")})

(defn #^{:doc     "Gitlab Group Actions.

Usage:
  gl_ga start-pipeline <api-url> <group-id> <access-token> [-r -n -b=<branch> -x=<excludes_file> -i=<includes_file> -t=<topic>]
  gl_ga create-tag <name> <message> <api-url> <group-id> <access-token> [-r -n -b=<branch> -x=<excludes_file> -i=<includes_file> -t=<topic>]
  gl_ga -h | --help
  gl_ga -v | --version

Options:
  -b=<branch>, --branch=<branch>      Use the given branch (or default branch with the magic ':default' keyword)
                                      for actions [default: :default].
  -r, --recursive                     Apply actions to all children of the given group.
  -x=<file>, --excludes-file=<file>   Skip projects matching RegExes from <excludes_file> (one per line).
                                      Excludes are applied against the whole project path (e.g. 'my-team/my-service')
                                      and need to match fully.
  -i=<file>, --includes-file=<file>   Only act on projects matching RegExes from <includes_file> (one per line).
                                      Includes are applied against the whole project path (e.g. 'my-team/my-service')
                                      and need to match fully. When omitted, all projects are kept.
                                      Includes run before excludes.
  -t=<topic>, --topic=<topic>         Filter projects by topic/topics. Accepts a comma-separated list. 
  -n, --dry-run                       Don't issue any mutating actions and just log them.
  -h, --help                          Show this screen.
  -v, --version                       Show version."
         :version "Gitlab Group Actions, version 0.1.0-SNAPSHOT"} ; TODO dynamic from project?
        parse-args [args]
  (let [arg-map (docopt (:doc (meta #'parse-args)) args)]
    (cond
      (nil? arg-map) (normalize (assoc arg-map "exit-status" 1))
      (arg-map "--help") (do (println (:doc (meta #'parse-args))) (normalize (assoc arg-map "exit-status" 0)))
      (arg-map "--version") (do (println (:version (meta #'parse-args))) (normalize (assoc arg-map "exit-status" 0)))
      :else (normalize arg-map))))


