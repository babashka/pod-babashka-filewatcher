# pod-babashka-filewatcher

## Build

```
$ cargo build --release
```

## Run

``` clojure
(require '[babashka.pods :as pods])
(pods/load-pod "target/release/pod-babashka-filewatcher")
(def chan (pod.babashka.filewatcher/watch "/tmp"))
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

## License

Copyright © 2020 Michiel Borkent

Distributed under the EPL License. See LICENSE.
