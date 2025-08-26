# Heylogs - communicate changes at scale

[![Download](https://img.shields.io/github/release/nbbrd/heylogs.svg)](https://github.com/nbbrd/heylogs/releases/latest)
[![Changes](https://img.shields.io/endpoint?url=https%3A%2F%2Fraw.githubusercontent.com%2Fnbbrd%2Fheylogs%2Fbadges%2Funreleased-changes.json)](https://github.com/nbbrd/heylogs/blob/develop/CHANGELOG.md)
[![Reproducible Builds](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/jvm-repo-rebuild/reproducible-central/master/content/com/github/nbbrd/heylogs/badge.json)](https://github.com/jvm-repo-rebuild/reproducible-central/blob/master/content/com/github/nbbrd/heylogs/README.md)

`Heylogs` is a tool designed to automate the validation and the release of changes in a [human-readable format](https://keepachangelog.com).  

Key points:

- Follow the [Keep a Changelog](https://keepachangelog.com/) format.
- Available as a [library](#library), a [command-line tool](#installation) and a [Maven plugin](#maven-plugin)
- Java 8 minimum requirement

Features:

- [Checks the format](#check-command) against an extensive [set of rules](#list-command).
- [Summarizes content](#scan-command) to provide a quick overview of a repository.
- [Filters and extracts](#extract-command) versions for publication or searching.
- [Modifies content](#release-command) to release unreleased changes.
- Manages [GitHub, GitLab and Forgejo](#forge-references) references.
- Validates [semantic, calendar and regex](#versioning-schemes) versioning schemes.
- Seamlessly integrates into [CI/CD pipelines](#github-action).


[ [Usage](#usage) | [Features](#features) | [Cookbook](#cookbook) | [Developing](#developing) | [Contributing](#contributing)  | [Licensing](#licensing) | [Related work](#related-work)]

## Usage

### Library

Heylogs is available as a **Java library**.  
The Java API is straightforward and has a single point of entry:
```java
Heylogs heylogs = Heylogs.ofServiceLoader();
Document flexmarkDocument = parseFileWithFlexmark(file);
List<Problem> problems = heylogs.checkFormat(flexmarkDocument);
...
```

> [!WARNING]
> This API is currently in beta and might change frequently.

<details>
<summary>Installation</summary>


```xml
<dependencies>
   <dependency>
      <groupId>com.github.nbbrd.heylogs</groupId>
      <artifactId>heylogs-api</artifactId>
      <version>_VERSION_</version>
   </dependency>
   <dependency>
      <groupId>com.github.nbbrd.heylogs</groupId>
      <artifactId>heylogs-ext-github</artifactId>
      <version>_VERSION_</version>
      <scope>runtime</scope>
   </dependency>
   ...
</dependencies>
```

</details>

### GitHub action

Most probably, one wants to check the `CHANGELOG.md` file, thus the command is as follows:

```yml
- uses: jbangdev/jbang-action@latest
  with:
    script: com.github.nbbrd.heylogs:heylogs-cli:_VERSION_:bin
    scriptargs: "check -o result.json"
```

An alternative is to use the Maven command-line:

```yml
- run: mvn com.github.nbbrd.heylogs:heylogs-maven-plugin:_VERSION_:check -D heylogs.outputFile=result.json
```

### Command-line tool

**Heylogs CLI** runs on any desktop operating system and requires Java 8 or later.  
It follows the Unix philosophy of [“Do one thing and do it well“](https://en.wikipedia.org/wiki/Unix_philosophy#Do_One_Thing_and_Do_It_Well) by performing a single function and beeing composable.

Composition example:  
1. download a changelog (`curl`)
2. summarize its content as json (`heylogs`)
3. colorize the output (`bat`).
```bash
curl -s https://raw.githubusercontent.com/olivierlacan/keep-a-changelog/main/CHANGELOG.md | heylogs scan - -f json | bat -l json
```

<details>
<summary>Installation</summary>

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

</details>

### Maven plugin

**Heylogs Maven plugin** allows the tool to be part of a Maven build workflow but can also be used as a standalone tool.

#### Examples

Check the changelog on every build:

```xml
<plugin>
    <groupId>com.github.nbbrd.heylogs</groupId>
    <artifactId>heylogs-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
            <inherited>false</inherited>
            <configuration>
                <versioning>semver</versioning>
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
                <executions>
                    <execution>
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

## Features

Heylogs provides several commands to interact with changelog files.
These commands can be used through the CLI, the Maven plugin or the Java API.
The examples below use the CLI for the sake of simplicity.
Command options are available through the `--help` option.

### Check command

The check command checks the format against an extensive set of rules.

```bash
$ heylogs check
CHANGELOG.md

  No problem
```

### Scan command

The scan command summarizes the content of changelog files to provide a quick overview of a repository.

```bash
$ heylogs scan
CHANGELOG.md
  Valid changelog
  Found 20 releases
  Ranging from 2022-09-08 to 2025-07-24
  Compatible with Semantic Versioning
  Forged with GitHub at https://github.com/nbbrd/heylogs
  Has 1 unreleased changes
```

### Extract command

The extract command filters and extracts versions for publication or searching.

```bash
$ heylogs extract --limit 1
## [Unreleased]

### Changed

- Switch to JSpecify [#143](https://github.com/nbbrd/heylogs/issues/143)

[Unreleased]: https://github.com/nbbrd/heylogs/compare/v0.11.1...HEAD
```

### Release command

The release command modifies the content of a changelog file to release unreleased changes.

### List command

The list command lists all the resources of the application.

```bash
$ heylogs list
Resources
  forge       main         forgejo                   Forgejo                   
  forge       main         github                    GitHub                    
  forge       main         gitlab                    GitLab                    
  format      automation   json                      JSON-serialized output    
  format      interaction  stylish                   Human-readable output     
  rule        extension    consistent-separator      Consistent separator      
  ...
  rule        versioning   semver                    Semantic Versioning format
  versioning  main         semver                    Semantic Versioning       
  
  32 resources found
```

### Forge references

Heylogs supports the following forge references:

|             | Commit | Compare | Issue | Request | Mention |
|:-----------:|:------:|:-------:|:-----:|:-------:|:-------:|
| **GitHub**  |   ✔    |    ✔    |   ✔   |    ✔    |    ✔    |
| **GitLab**  |   ✔    |    ✔    |   ✔   |    ✔    |    ✔    |
| **Forgejo** |   ✔    |    ✔    |   ✔   |    ✔    |    ✔    |

### Versioning schemes

Heylogs validates several versioning schemes:

|    ID    | Description                                | Example      | Argument       |
|:--------:|--------------------------------------------|--------------|----------------|
| `semver` | [Semantic versioning](https://semver.org/) | `1.0.0`      | -              |
| `calver` | [Calendar versioning](https://calver.org/) | `2023.04.01` | calver pattern |
| `regex`  | Custom regex-based versioning              | `X13`        | regex pattern  |

Heylogs can detect the versioning scheme automatically when scanning a changelog, 
but you need to specify it explicitly to enable validation.

Examples:
- `$ heylogs check -v semver`  
- `$ heylogs check -v calver:YYYY.MM.DD`  
- `$ heylogs check -v regex:X\d+`

## Cookbook

### Badges

Heylogs makes it possible to generate badges for the unreleased changes of a changelog using a [GitHub workflow](https://github.com/nbbrd/heylogs/blob/develop/.github/workflows/heylogs.yml) and the [shields.io API](https://shields.io/badges/endpoint-badge).  

This workflow has the following steps:
1. it [summarizes](https://github.com/nbbrd/heylogs#maven-plugin) the changelog content into a json file
2. it converts this json file to a format that [shields.io](https://shields.io/badges/endpoint-badge) can understand
3. it pushes the result to a [dedicated branch](https://github.com/nbbrd/heylogs/tree/badges)

Here are some examples:

![default](https://img.shields.io/endpoint?url=https%3A%2F%2Fraw.githubusercontent.com%2Fnbbrd%2Fheylogs%2Fbadges%2Funreleased-changes.json)  
![with custom label](https://img.shields.io/endpoint?url=https%3A%2F%2Fraw.githubusercontent.com%2Fnbbrd%2Fheylogs%2Fbadges%2Funreleased-changes.json&label=changelog)  
![without icon](https://img.shields.io/endpoint?url=https%3A%2F%2Fraw.githubusercontent.com%2Fnbbrd%2Fheylogs%2Fbadges%2Funreleased-changes.json&label=changelog&logo=none)  
![without label](https://img.shields.io/endpoint?url=https%3A%2F%2Fraw.githubusercontent.com%2Fnbbrd%2Fheylogs%2Fbadges%2Funreleased-changes.json&label=%20)

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
- [GIT Changelog Merge Driver](https://github.com/maven-flow/changelog-merge-driver) - does a semantic merge of two changelogs.

[WINDOWS]: https://img.shields.io/badge/-WINDOWS-068C09

[MACOS]: https://img.shields.io/badge/-MACOS-5319E7

[LINUX]: https://img.shields.io/badge/-LINUX-BC0250

[DOCKER]: https://img.shields.io/badge/-DOCKER-E2BC4A

[GITHUB]: https://img.shields.io/badge/-GITHUB-e4e669

[MAVEN]: https://img.shields.io/badge/-MAVEN-e4e669

[GRADLE]: https://img.shields.io/badge/-GRADLE-F813F7
