(ns pod.babashka.filewatcher-test
  (:require [babashka.pods :as pods]
            [clojure.core.async :as async]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [clojure.string :as str]))

(pods/load-pod "target/release/pod-babashka-filewatcher")
(require '[pod.babashka.filewatcher :as fw])

(deftest filewatcher-test
  (let [tmp-dir (io/file (System/getProperty "java.io.tmpdir"))
        chan (fw/watch (.getPath tmp-dir))
        txt-file (io/file tmp-dir "foo.txt")]
    (.delete txt-file)
    (loop [actions [#(spit txt-file "contents")
                    #(spit txt-file "contents" :append true)
                    #(.delete txt-file)
                    (constantly true)]
           events []]
      (if-let [action (first actions)]
        (do
          (action)
          (let [event (async/<!! chan)]
            (recur (rest actions)
                   (conj events event))))
        (do (is (= "create" (first (map :type events))))
            (is (every? #(str/ends-with? % "foo.txt")
                        (map :path events))))))))

(deftest filewatcher-opts-test
  (let [tmp-dir (io/file (System/getProperty "java.io.tmpdir"))
        chan (fw/watch (.getPath tmp-dir) {:delay-ms 0})
        txt-file (io/file tmp-dir "foo.txt")]
    (.delete txt-file)
    (loop [actions [#(spit txt-file "contents")]
           events []]
      (if-let [action (first actions)]
        (do
          (action)
          (let [event (async/<!! chan)]
            (recur (rest actions)
                   (conj events event))))
        (is (pos? (count events)))))))
