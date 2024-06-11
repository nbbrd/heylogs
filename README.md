# heylogs

[![Download](https://img.shields.io/github/release/nbbrd/heylogs.svg)](https://github.com/nbbrd/heylogs/releases/latest)
[![Changes](https://img.shields.io/endpoint?url=https%3A%2F%2Fraw.githubusercontent.com%2Fnbbrd%2Fheylogs%2Fbadges%2Funreleased-changes.json)](https://github.com/nbbrd/heylogs/blob/develop/CHANGELOG.md)

`heylogs` is a set of tools to deal with the [keep-a-changelog format](https://keepachangelog.com),
a changelog format designed to be human-readable.
It can be used as a linter in interactive sessions and automations.

Key points:

- Available as a [library](#library), a [command-line tool](#installation) and a [Maven plugin](#maven-plugin)
- Java 8 minimum requirement

Features:

- Checks format
- Summarizes content
- Extracts versions

[ [Library](#library) | [Command-line tool](#command-line-tool) | [Maven plugin](#maven-plugin) | [Badges](#badges) | [Developing](#developing) | [Contributing](#contributing)  | [Licensing](#licensing) | [Related work](#related-work)]

## Library

`WIP`

## Command-line tool

**Heylogs CLI** runs on any desktop operating system such as Microsoft Windows, 
Solaris OS, Apple macOS, Ubuntu and other various Linux distributions. 
It requires a Java SE Runtime Environment (JRE) version 8 or later to run on such as OpenJDK.

It provides the following commands:

| Name      | Description                     |
|-----------|---------------------------------|
| `scan`    | Summarize changelog content     |
| `check`   | Check changelog format          |
| `extract` | Extract versions from changelog |
| `list`    | List available resources        |

It follows the Unix philosophy of [“Do one thing and do it well“](https://en.wikipedia.org/wiki/Unix_philosophy#Do_One_Thing_and_Do_It_Well) by performing a single function and beeing composable.

Composition example:  
1. download a changelog (`curl`)
2. summarize its content as json (`heylogs`)
3. colorize the output (`bat`).
```bash
curl -s https://raw.githubusercontent.com/olivierlacan/keep-a-changelog/main/CHANGELOG.md | heylogs scan - -f json | bat -l json
```

### Using in a GitHub action

![GITHUB]

Most probably, one wants to check the `CHANGELOG.md` file, thus the command is as follows:

```yml
- uses: jbangdev/jbang-action@v0.110.1
  with:
    script: com.github.nbbrd.heylogs:heylogs-cli:_VERSION_:bin
    scriptargs: "check CHANGELOG.md"
```

### Installation

The easiest way of installing the CLI is to use a package manager.  
Each operating system has its own manager. See the list below for specific instructions.

#### Scoop

![WINDOWS]

```shell
scoop bucket add nbbrd https://github.com/nbbrd/scoop-nbbrd.git
scoop install heylogs
```

#### Homebrew

![MACOS] ![LINUX]

```shell
brew install nbbrd/tap/heylogs
```

#### JBang

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

#### Maven command-line

![WINDOWS] ![MACOS] ![LINUX] ![GITHUB]

```shell
mvn dependency:copy -Dartifact=com.github.nbbrd.heylogs:heylogs-cli:_VERSION_:jar:bin -DoutputDirectory=. -Dmdep.stripVersion -q
java -jar heylogs-cli-bin.jar <command> [<args>]
```

#### Zero installation

![WINDOWS] ![MACOS] ![LINUX] ![GITHUB]

The CLI is a single executable jar, so it doesn't need to be installed to be used.  
To use the CLI without installing it:

1. Download the latest jar binary (`heylogs-_VERSION_-bin.jar`) at:  
   [https://github.com/nbbrd/heylogs/releases/latest](https://github.com/nbbrd/heylogs/releases/latest)
2. Run this jar by calling:  
   `java -jar heylogs-cli-_VERSION_-bin.jar <command> [<args>]`

## Maven plugin

**Heylogs Maven plugin** allows the tool to be part of a Maven build workflow.

It provides the following goals:

| Name      | Description                     |
|-----------|---------------------------------|
| `scan`    | Summarize changelog content     |
| `check`   | Check changelog format          |
| `extract` | Extract versions from changelog |
| `list`    | List available resources        |

### Examples

Check the changelog on every build:

```xml
<plugin>
    <groupId>com.github.nbbrd.heylogs</groupId>
    <artifactId>heylogs-maven-plugin</artifactId>
    <version>${heylogs.version}</version>
    <executions>
        <execution>
            <id>check-changelog</id>
            <goals>
                <goal>check</goal>
            </goals>
            <inherited>false</inherited>
            <configuration>
                <semver>true</semver>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Extract the latest version from the changelog during a release:

```xml
<profile>
    <id>release</id>
    <build>
        <plugins>
            <plugin>
                <groupId>com.github.nbbrd.heylogs</groupId>
                <artifactId>heylogs-maven-plugin</artifactId>
                <version>${heylogs.version}</version>
                <executions>
                    <execution>
                        <id>extract-changelog</id>
                        <goals>
                            <goal>extract</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</profile>
```

## Badges

Heylogs make it possible to generate nice badges using a [GitHub workflow](https://github.com/nbbrd/heylogs/blob/develop/.github/workflows/heylogs.yml) and the [shields.io API](https://shields.io/badges/endpoint-badge).  
Here are some examples:

![Endpoint Badge](https://img.shields.io/endpoint?url=https%3A%2F%2Fraw.githubusercontent.com%2Fnbbrd%2Fheylogs%2Fbadges%2Funreleased-changes.json)  
![Endpoint Badge](https://img.shields.io/endpoint?url=https%3A%2F%2Fraw.githubusercontent.com%2Fnbbrd%2Fheylogs%2Fbadges%2Funreleased-changes.json&label=changelog)  
![Endpoint Badge](https://img.shields.io/endpoint?url=https%3A%2F%2Fraw.githubusercontent.com%2Fnbbrd%2Fheylogs%2Fbadges%2Funreleased-changes.json&label=changelog&logo=none)  
![Endpoint Badge](https://img.shields.io/endpoint?url=https%3A%2F%2Fraw.githubusercontent.com%2Fnbbrd%2Fheylogs%2Fbadges%2Funreleased-changes.json&label=%20)

## Developing

This project is written in Java and uses [Apache Maven](https://maven.apache.org/) as a build tool.  
It requires [Java 8 as minimum version](https://whichjdk.com/) and all its dependencies are hosted
on [Maven Central](https://search.maven.org/).

The code can be build using any IDE or by just type-in the following commands in a terminal:

```shell
git clone https://github.com/nbbrd/heylogs.git
cd heylogs
mvn clean install
```

## Contributing

Any contribution is welcome and should be done through pull requests and/or issues.

## Licensing

The code of this project is licensed under
the [European Union Public Licence (EUPL)](https://joinup.ec.europa.eu/page/eupl-text-11-12).

## Related work

This project is not the only one that deals with keep-a-changelog format.  
Here is a non-exhaustive list of related work:

- [clparse](https://github.com/marcaddeo/clparse)

[WINDOWS]: https://img.shields.io/badge/-WINDOWS-068C09

[MACOS]: https://img.shields.io/badge/-MACOS-5319E7

[LINUX]: https://img.shields.io/badge/-LINUX-BC0250

[DOCKER]: https://img.shields.io/badge/-DOCKER-E2BC4A

[GITHUB]: https://img.shields.io/badge/-GITHUB-e4e669

[MAVEN]: https://img.shields.io/badge/-MAVEN-e4e669

[GRADLE]: https://img.shields.io/badge/-GRADLE-F813F7
