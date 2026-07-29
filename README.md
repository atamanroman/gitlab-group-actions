# gitlab-group-actions

Run actions against all repos in a gitlab group (and subgroups).

Features:

- start a pipeline for a given branch (or default)
- create a tag on a given branch (or default)

## Requirements

`$ brew install clojure leiningen`

## Usage

This tool is documented with [docopt](http://docopt.org/).

Run `$ lein run --` to see the docs.

Examples:

```
$ lein run -- create-tag 1.0.0 "my amazing tag" https://gitlab.com/api/v4 186 $TOKEN --topic=releasable --dry-run --excludes-file=excludes.txt
$ lein run -- start-pipeline https://gitlab.com/api/v4 186 $TOKEN --topic=releasable --dry-run --branch=master --excludes-file=excludes.txt
$ lein run -- create-tag 1.0.0 "release" https://gitlab.com/api/v4 186 $TOKEN --topic=releasable --includes-file=includes.txt
```

## Filtering projects

Three filters are available and can be combined:

- `--topic=<topic>`: only act on projects tagged with the given GitLab topic.
- `--excludes-file=<file>`: skip projects whose path matches any regex in the file.
- `--includes-file=<file>`: keep only projects whose path matches at least one regex in the file. When omitted, all projects are kept.

Each file holds one regex per line. Regexes match the full project path (e.g. `my-team/my-service`). Includes run before excludes, so a project listed in both is excluded.
