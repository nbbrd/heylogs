# Command-line tool

Heylogs CLI is a cross-platform tool (Java 8+) for validating and managing changelogs. It enforces Keep a Changelog format, supports custom rules, and integrates with CI/CD pipelines.

## Workflow example

This example shows a typical changelog and release workflow using Heylogs CLI with real commands and options.

```bash
# 1. Create a heylogs.properties file to set versioning and tagging options
echo "versioning=semver" >> heylogs.properties
echo "tagging=prefix:v" >> heylogs.properties

# 2. Initialize a new changelog file with project URL
heylogs init --project-url https://github.com/nbbrd/heylogs

# 3. Push a new 'Added' change entry
heylogs push -y added -m "Add support for custom CLI workflows."

# 4. Push a 'Fixed' change entry
heylogs push -y fixed -m "Fix bug in changelog parser."

# 5. Fetch an issue from GitHub and add as an 'Added' change (using issue ref)
heylogs fetch -y added -i "#1"

# 6. Check the changelog for format and rule compliance, with a custom rule severity
heylogs check --rule dot-space-link-style:WARN --rule no-empty-group:OFF

# 7. Extract the latest version to a separate file (limit 1)
heylogs extract --limit 1 --output CHANGELOG-latest.md

# 8. Set a summary for the 'Unreleased' section (recap changes before release)
heylogs note -m "This release introduces advanced CLI scenarios."

# 9. Release a new version (e.g., 1.0.0)
heylogs release --ref 1.0.0

# 10. Summarize the changelog content as JSON and write to a file
heylogs scan --output summary.json --format json
```

## Composition example

The CLI follows the Unix philosophy of ["Do one thing and do it well"](https://en.wikipedia.org/wiki/Unix_philosophy#Do_One_Thing_and_Do_It_Well) by performing a single function and being composable.

1. download a changelog (`curl`)
2. summarize its content as json (`heylogs`)
3. colorize the output (`bat`)

```bash
curl -s https://raw.githubusercontent.com/olivierlacan/keep-a-changelog/main/CHANGELOG.md \    # 1️⃣
  | heylogs scan - -f json \                                                                   # 2️⃣
  | bat -l json                                                                                # 3️⃣
```

## Installation

The easiest way of installing the CLI is to use a package manager.
Each operating system has its own manager. See the list below for specific instructions.

### Scoop (Windows)

```shell
scoop bucket add nbbrd https://github.com/nbbrd/scoop-nbbrd.git
scoop install heylogs
```

### Homebrew (macOS and Linux)

```shell
brew install nbbrd/tap/heylogs
```

### JBang (almost anywhere)

The CLI can be run by JBang almost anywhere using one of these options:
- Specific version (Maven coordinates): `com.github.nbbrd.heylogs:heylogs-cli:_VERSION_:bin`
- Latest version (JBang catalog): `heylogs@nbbrd`

On Windows, macOS, Linux:
```shell
jbang com.github.nbbrd.heylogs:heylogs-cli:_VERSION_:bin <command> [<args>]
```

On Docker:
```shell
docker run -v `pwd`:/ws --workdir=/ws jbangdev/jbang-action com.github.nbbrd.heylogs:heylogs-cli:_VERSION_:bin <command> [<args>]
```

On GitHub Actions:
```yml
- uses: jbangdev/jbang-action@v0.110.1
  with:
    script: com.github.nbbrd.heylogs:heylogs-cli:_VERSION_:bin
    scriptargs: "<command> [<args>]"
```

_Note that the trust parameter is required if the catalog is used instead of the Maven coordinates:
`trust: https://github.com/nbbrd/jbang-catalog`_

### Maven command-line (almost anywhere)

```shell
mvn dependency:copy -Dartifact=com.github.nbbrd.heylogs:heylogs-cli:_VERSION_:jar:bin -DoutputDirectory=. -Dmdep.stripVersion -q
java -jar heylogs-cli-bin.jar <command> [<args>]
```

### Zero installation

The CLI is a single executable jar, so it doesn't need to be installed to be used.
To use the CLI without installing it:

1. Download the latest jar binary (`heylogs-_VERSION_-bin.jar`) at:
   [https://github.com/nbbrd/heylogs/releases/latest](https://github.com/nbbrd/heylogs/releases/latest)
2. Run this jar by calling:
   `java -jar heylogs-cli-_VERSION_-bin.jar <command> [<args>]`

[WINDOWS]: https://img.shields.io/badge/-WINDOWS-068C09
[MACOS]: https://img.shields.io/badge/-MACOS-5319E7
[LINUX]: https://img.shields.io/badge/-LINUX-BC0250
[DOCKER]: https://img.shields.io/badge/-DOCKER-E2BC4A
[GITHUB]: https://img.shields.io/badge/-GITHUB-e4e669

### Java system properties

The CLI is running on the Java runtime.
It is possible to configure the runtime by setting Java system properties with the following syntax:
`heylogs <command> -D<property>=<value> [options]`

---

[← Back to README](../README.md)
