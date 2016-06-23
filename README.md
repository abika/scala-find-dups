# scala-find-dups

Yet another tool to find duplicate files (and delete them if wanted). This one written in Scala so that Odersky didn't put all programming features ever invented into one language for nothing.

### Duplicate identification

Duplicates are identified by their whole file content. But I/O optimized: content is only compared if the file size matches.

### Regex option

With `-x <regex>` you can narrow the search for duplicates. All filenames which will be marked as duplicates must match the regular expression. Note that this restriction is only used after scanning all files and for marking a file as orignal or duplicate. 

### Usage example

To find all duplicate files that have a unix timestamp as filename in the folder *cirno* (and all subfolders) run
```
sbt "run -r -x [0-9]{13}\.[a-zA-Z]{3,4} cirno"
```
Add `-D` to get rid of redundant fairies.
