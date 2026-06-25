(ns gitlab-group-actions.cli-test
  (:require [clojure.test :refer :all]
            [gitlab-group-actions.cli :refer :all]
            [clojure.java.io :as io]))

(deftest version
  (testing "-v"
    (let [args (parse-args ["-v"])]
      (is (= :exit (:action args)))
      (is (= 0 (:exit-status args)))))
  (testing "--version"
    (let [args (parse-args ["--version"])]
      (is (= :exit (:action args)))
      (is (= 0 (:exit-status args))))))

(deftest help
  (testing "-h"
    (let [args (parse-args ["-h"])]
      (is (= :exit (:action args)))
      (is (= 0 (:exit-status args)))))

  (testing "--help"
    (let [args (parse-args ["-h"])]
      (is (= :exit (:action args)))
      (is (= 0 (:exit-status args))))))

(deftest bad-input
  (let [args (parse-args ["foo"])]
    (is (= :exit (:action args)))
    (is (= 1 (:exit-status args)))))

(deftest start-pipeline
  (testing "--dry-run"
    (let [args (parse-args ["start-pipeline" "a" "123" "c" "--dry-run"])]
      (is (= true (:dry-run args)))
      (is (= :start-pipeline (:action args)))
      (is (= false (:recursive args)))
      (is (= "a" (:api-url args)))
      (is (= 123 (:group-id args)))
      (is (= "c" (:access-token args)))
      (is (= :default (:branch args)))
      (is (nil? (:exit-status args)))))

  (testing "-r"
    (let [args (parse-args ["start-pipeline" "a" "123" "c" "-r"])]
      (is (= false (:dry-run args)))
      (is (= :start-pipeline (:action args)))
      (is (= true (:recursive args)))
      (is (= "a" (:api-url args)))
      (is (= 123 (:group-id args)))
      (is (= "c" (:access-token args)))
      (is (= :default (:branch args)))
      (is (nil? (:exit-status args)))))

  (testing "create-tag args are not set"
    (let [args (parse-args ["start-pipeline" "a" "123" "c"])]
      (is (nil? (:tag-name args)))
      (is (nil? (:tag-message args)))))

  (testing "exclude-file is read"
    (let [args (parse-args ["start-pipeline" "a" "123" "c" "--excludes-file=test/test_excludes.txt"])]
      (is (= 2 (count (:excludes args))))
      (is (= "foo" (.pattern (nth (:excludes args) 0))))
      (is (= "bar" (.pattern (nth (:excludes args) 1))))))

  (testing "includes-file is read"
    (let [args (parse-args ["start-pipeline" "a" "123" "c" "--includes-file=test/test_includes.txt"])]
      (is (= 2 (count (:includes args))))
      (is (= "keep/.*" (.pattern (nth (:includes args) 0))))
      (is (= "also/.*" (.pattern (nth (:includes args) 1))))))

  (testing "without includes-file :includes is nil"
    (let [args (parse-args ["start-pipeline" "a" "123" "c"])]
      (is (nil? (:includes args))))))

(deftest create-tag
  (testing "create-tag"
    (let [args (parse-args ["create-tag" "1.2.3" "Release 1.2.3" "a" "123" "c"])]
      (is (= :create-tag (:action args)))
      (is (= "1.2.3" (:tag-name args)))
      (is (= "Release 1.2.3" (:tag-message args)))
      (is (= false (:recursive args)))
      (is (= false (:dry-run args)))
      (is (= "a" (:api-url args)))
      (is (= 123 (:group-id args)))
      (is (= "c" (:access-token args)))
      (is (= :default (:branch args)))
      (is (nil? (:exit-status args))))))
