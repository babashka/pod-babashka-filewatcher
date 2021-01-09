(ns pod.babashka.filewatcher-test
  (:require [babashka.pods :as pods]
            [clojure.core.async :as async]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]))

(pods/load-pod "target/release/pod-babashka-filewatcher")
(require '[pod.babashka.filewatcher :as fw])

(deftest filewatcher-test
  (let [tmp-dir (.toFile
                 (java.nio.file.Files/createTempDirectory
                  "watch-1"
                  (into-array java.nio.file.attribute.FileAttribute [])))
        chan (async/chan)
        cb (fn [result]
             ;; (prn :result-fw result)
             (async/put! chan result))
        txt-file (io/file tmp-dir "foo.txt")]
    (.delete txt-file)
    (fw/watch (.getPath tmp-dir) cb)
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
        (do nil ;; (run! prn events)
            (is (= 3 (count (filter #(str/ends-with? % "foo.txt")
                                    (map :path events)))))
            (prn :end-filewatcher-test))))))

(deftest filewatcher-opts-test
  (let [tmp-dir (.toFile
                 (java.nio.file.Files/createTempDirectory
                  "watch-2"
                  (into-array java.nio.file.attribute.FileAttribute [])))
        chan (async/chan)
        cb (fn [result]
             ;; (prn :result-opts result)
             (async/put! chan result))
        txt-file (io/file tmp-dir "foo.txt")]
    (.delete txt-file)
    (fw/watch (.getPath tmp-dir) cb {:delay-ms 0})
    (loop [actions [#(spit txt-file "contents")]
           events []]
      (if-let [action (first actions)]
        (do
          (action)
          (let [event (async/<!! chan)]
            (recur (rest actions)
                   (conj events event))))
        (is (pos? (count events)))))
    (prn :end)))
