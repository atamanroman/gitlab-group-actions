(ns gitlab-group-actions.projects
  (:require [gitlab-group-actions.api :as api]
            [clojure.tools.logging :as log]))

(defn- log-projects [projects] (doseq [project projects]
                                 (log/debugf "Found project: %s"
                                             {:name           (:path_with_namespace project)
                                              :id             (:id project)
                                              :default_branch (:default_branch project)
                                              :url            (:web_url project)})))

(defn- is-excluded [project exclude-regexes] (seq (filter (fn [re] (if (re-matches re (:path_with_namespace project)) re)) exclude-regexes)))
(defn- remove-excluded [projects exclude-regexes] (filter #(let [excludes (is-excluded % exclude-regexes)]
                                                             (if excludes
                                                               (do (println "Excluded:" (:path_with_namespace %) (str "(matches " excludes ")"))
                                                                   false)
                                                               true))
                                                          projects))

(defn- matches-includes [project include-regexes] (seq (filter (fn [re] (if (re-matches re (:path_with_namespace project)) re)) include-regexes)))
(defn- keep-included [projects include-regexes] (if (empty? include-regexes)
                                                  projects
                                                  (filter #(let [matches (matches-includes % include-regexes)]
                                                             (if matches
                                                               true
                                                               (do (println "Not included:" (:path_with_namespace %))
                                                                   false)))
                                                          projects)))

(defn get-projects
  ([{:keys [:access-token :api-url :group-id :recursive :topic :excludes :includes]} page]
   (let [resp (api/get
               access-token
               "%s/groups/%d/projects?include_subgroups=%b&topic=%s&simple=true&archived=false&per_page=100&page=%d"
               [api-url group-id recursive topic page])
         body (:body resp)
         total_pages (Integer/parseInt (:x-total-pages (:headers resp)))]
     (if (> total_pages page) (throw (new IllegalStateException "OMG THERE ARE MORE PAGEZZZ"))) ; TODO
     (log-projects body)
     (-> body (keep-included includes) (remove-excluded excludes))))
  ([gitlab-options] (get-projects gitlab-options 1)))

(defn get-target-branch [project cli-branch]
  (if
   (= cli-branch :default) (:default_branch project) cli-branch))
