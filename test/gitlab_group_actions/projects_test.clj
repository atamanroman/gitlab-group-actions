(ns gitlab-group-actions.projects-test
  (:require [clojure.test :refer :all]
            [gitlab-group-actions.projects :refer :all]))

(deftest remove-excluded-test
  (let [cut #'gitlab-group-actions.projects/remove-excluded]
    (testing "regex matches marks as excluded"
      (is (empty? (cut [{:id 1 :path_with_namespace "foo"}] [#"foo"])))
      (is (empty? (cut [{:id 1 :path_with_namespace "foo"}] [#".*"])))
      (is (empty? (cut [{:id 1 :path_with_namespace "foo/bar"}] [#".*"])))
      (is (empty? (cut [{:id 1 :path_with_namespace "foo/bar"}] [#"foo/.*"]))))
    (testing "regex miss marks not as excluded"
      (is (= 1 (count (cut [{:id 1 :path_with_namespace "foo"}] [#"bar"]))))
      (is (= 1 (count (cut [{:id 1 :path_with_namespace "foo/foo"}] [#"bar/.*"])))))
    (testing "mixing excluded/not excluded and multiple regexes"
      (let [projects (cut [{:id 1 :path_with_namespace "foo/foo"}
                           {:id 2 :path_with_namespace "bar/bar"}
                           {:id 3 :path_with_namespace "baz/baz"}] [#"foo/.*" #"bar/.*"])]
        (is (= 1 (count projects)))
        (is (= 3 (:id (first projects))))))))

(deftest keep-included-test
  (let [cut #'gitlab-group-actions.projects/keep-included]
    (testing "nil or empty includes keeps everything (no whitelist active)"
      (is (= 1 (count (cut [{:id 1 :path_with_namespace "foo"}] nil))))
      (is (= 1 (count (cut [{:id 1 :path_with_namespace "foo"}] [])))))
    (testing "regex match keeps project"
      (is (= 1 (count (cut [{:id 1 :path_with_namespace "foo"}] [#"foo"]))))
      (is (= 1 (count (cut [{:id 1 :path_with_namespace "foo/bar"}] [#"foo/.*"])))))
    (testing "no regex match drops project"
      (is (empty? (cut [{:id 1 :path_with_namespace "foo"}] [#"bar"])))
      (is (empty? (cut [{:id 1 :path_with_namespace "foo/foo"}] [#"bar/.*"]))))
    (testing "any of multiple regexes matching keeps project"
      (let [projects (cut [{:id 1 :path_with_namespace "foo/foo"}
                           {:id 2 :path_with_namespace "bar/bar"}
                           {:id 3 :path_with_namespace "baz/baz"}] [#"foo/.*" #"bar/.*"])]
        (is (= 2 (count projects)))
        (is (= #{1 2} (set (map :id projects))))))))

(deftest get-target-branch-test
  (testing "explicit branch wins over default branch"
    (is (= "staging" (gitlab-group-actions.projects/get-target-branch {:default_branch "main"} "staging")))
    (is (= "main" (gitlab-group-actions.projects/get-target-branch {:default_branch "main"} :default)))))
