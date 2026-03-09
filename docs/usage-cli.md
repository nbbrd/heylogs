# Command-Line Tool

**Heylogs CLI** runs on any desktop operating system and requires Java 8 or later.  
It follows the Unix philosophy of ["Do one thing and do it well"](https://en.wikipedia.org/wiki/Unix_philosophy#Do_One_Thing_and_Do_It_Well) by performing a single function and being composable.

## Composition Example

1. download a changelog (`curl`)
2. summarize its content as json (`heylogs`)
3. colorize the output (`bat`)

```bash
curl -s https://raw.githubusercontent.com/olivierlacan/keep-a-changelog/main/CHANGELOG.md | heylogs scan - -f json | bat -l json
```

## Installation

The easiest way of installing the CLI is to use a package manager.  
Each operating system has its own manager. See the list below for specific instructions.

### Scoop

![WINDOWS]

```shell
scoop bucket add nbbrd https://github.com/nbbrd/scoop-nbbrd.git
scoop install heylogs
```

### Homebrew

![MACOS] ![LINUX]

```shell
brew install nbbrd/tap/heylogs
```

### JBang

The CLI can be run by JBang almost anywhere using one of these options:
- Specific version (Maven coordinates): `com.github.nbbrd.heylogs:heylogs-cli:_VERSION_:bin`
- Latest version (JBang catalog): `heylogs@nbbrd`

![WINDOWS] ![MACOS] ![LINUX]

```shell
jbang com.github.nbbrd.heylogs:heylogs-cli:_VERSION_:bin <command> [<args>]
```

![DOCKER]

```shell
docker run -v `pwd`:/ws --workdir=/ws jbangdev/jbang-action com.github.nbbrd.heylogs:heylogs-cli:_VERSION_:bin <command> [<args>]
```

![GITHUB]

```yml
- uses: jbangdev/jbang-action@v0.110.1
  with:
    script: com.github.nbbrd.heylogs:heylogs-cli:_VERSION_:bin
    scriptargs: "<command> [<args>]"
```

_Note that the trust parameter is required if the catalog is used instead of the Maven coordinates:  
`trust: https://github.com/nbbrd/jbang-catalog`_

### Maven command-line

![WINDOWS] ![MACOS] ![LINUX] ![GITHUB]

```shell
mvn dependency:copy -Dartifact=com.github.nbbrd.heylogs:heylogs-cli:_VERSION_:jar:bin -DoutputDirectory=. -Dmdep.stripVersion -q
java -jar heylogs-cli-bin.jar <command> [<args>]
```

### Zero installation

![WINDOWS] ![MACOS] ![LINUX] ![GITHUB]

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

[← Back to Usage](usage.md)

