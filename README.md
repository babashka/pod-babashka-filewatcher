# pod-babashka-filewatcher

A [babashka pod](https://github.com/borkdude/babashka/blob/master/doc/pods.md)
for watching files. Implemented using the Rust
[notify](https://github.com/notify-rs/notify) library.

## Run

Run in [babashka](https://github.com/borkdude/babashka/) or using the
[babashka.pods](https://github.com/babashka/babashka.pods) library on the JVM.

``` clojure
(require '[babashka.pods :as pods])
(pods/load-pod "target/release/pod-babashka-filewatcher")

(require '[pod.babashka.filewatcher :as fw])

(def chan (fw/watch "/tmp"))

(require '[clojure.core.async :as async])
(loop [] (prn (async/<!! chan)) (recur))
```

As a result of the following terminal sequence:

``` shell
$ touch created.txt
$ mv created.txt created_renamed.txt
$ chmod -w created_renamed.txt
$ chmod +w created_renamed.txt
$ echo "foo" >> created_renamed.txt
```

the following will be printed:

``` clojure
{:path "/private/tmp/created.txt", :type "create"}
{:path "/private/tmp/created.txt", :type "notice/remove"}
{:dest "/private/tmp/created_renamed.txt", :path "/private/tmp/created.txt", :type "rename"}
{:path "/private/tmp/created_renamed.txt", :type "chmod"}
{:path "/private/tmp/created_renamed.txt", :type "chmod"}
{:path "/private/tmp/created_renamed.txt", :type "notice/write"}
{:path "/private/tmp/created_renamed.txt", :type "write"}
```

## Build

```
$ cargo build --release
```

## License

Copyright © 2020 Michiel Borkent

Distributed under the EPL License. See LICENSE.
