# Heylogs - communicate changes at scale

[![Download](https://img.shields.io/github/release/nbbrd/heylogs.svg)](https://github.com/nbbrd/heylogs/releases/latest)
[![Changes](https://img.shields.io/endpoint?url=https%3A%2F%2Fraw.githubusercontent.com%2Fnbbrd%2Fheylogs%2Fbadges%2Funreleased-changes.json)](https://github.com/nbbrd/heylogs/blob/develop/CHANGELOG.md)
[![Reproducible Builds](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/jvm-repo-rebuild/reproducible-central/master/content/com/github/nbbrd/heylogs/badge.json)](https://github.com/jvm-repo-rebuild/reproducible-central/blob/master/content/com/github/nbbrd/heylogs/README.md)

`Heylogs` is a tool designed to automate the validation and the release of changes in a [human-readable format](https://keepachangelog.com).

Key points:

- Follow the [Keep a Changelog](https://keepachangelog.com/) format.
- Available as a [library](docs/usage-library.md), a [command-line tool](docs/usage-cli.md), a [Maven plugin](docs/usage-maven-plugin.md) and [Maven Enforcer rules](docs/usage-enforcer-rules.md).
- Java 8 minimum requirement

Features:

* [Create](docs/feature-init.md) changelog files, and [list](docs/feature-list.md) available resources and rules.
* [Validate](docs/feature-check.md) changelog format, [summarize](docs/feature-scan.md) content, and [extract](docs/feature-extract.md) or filter versions for publication or searching.
* [Add](docs/feature-push.md), [fetch](docs/feature-fetch.md), or set [summaries](docs/feature-note.md) for changes; [release](docs/feature-release.md) new versions or [yank](docs/feature-yank.md) problematic releases.
* Integrate with [GitHub](docs/feature-forge.md), [GitLab](docs/feature-forge.md), and [Forgejo](docs/feature-forge.md); support [semantic](docs/feature-versioning.md), [calendar](docs/feature-versioning.md), and [regex](docs/feature-versioning.md) versioning; and handle [tag](docs/feature-tagging.md) prefix strategies.
* Automate workflows in [CI/CD](docs/usage-pipelines.md) pipelines and support hierarchical [configuration](docs/feature-config-file.md) via `heylogs.properties` files.

[ [Usage](#usage) | [Features](#features) | [Cookbook](#cookbook) | [Developing](#developing) | [Contributing](#contributing)  | [Licensing](#licensing) | [Related work](#related-work)]

## Usage

Heylogs is available in multiple formats to fit your workflow:

- **[Library](docs/usage-library.md)** - Java API for programmatic access
- **[Command-line tool](docs/usage-cli.md)** - Command-line tool for any platform
- **[Maven plugin](docs/usage-maven-plugin.md)** - Maven build integration
- **[Maven Enforcer rules](docs/usage-enforcer-rules.md)** - Enforce changelog quality in builds
- **[CI/CD pipelines](docs/usage-pipelines.md)** - Integrate into CI/CD environments

## Features

Heylogs provides several **commands** to interact with changelog files:

- **[init](docs/feature-init.md)** - Create a new changelog file with a template
- **[check](docs/feature-check.md)** - Validate changelog format against an extensive set of rules
- **[scan](docs/feature-scan.md)** - Summarize changelog content and metadata
- **[list](docs/feature-list.md)** - Display all available resources and rules
- **[extract](docs/feature-extract.md)** - Filter and extract specific versions
- **[push](docs/feature-push.md)** - Insert a change entry to the Unreleased section
- **[fetch](docs/feature-fetch.md)** - Fetch a change from a forge issue or pull request into Unreleased
- **[note](docs/feature-note.md)** - Set or update the summary text after the Unreleased header
- **[release](docs/feature-release.md)** - Convert unreleased changes into a new release
- **[yank](docs/feature-yank.md)** - Mark an existing release as yanked

Heylogs also provides some **customizations** to adapt to various workflows:

- **[forge](docs/feature-forge.md)** - Support for GitHub, GitLab, and Forgejo specific features
- **[versioning](docs/feature-versioning.md)** - Validate version numbers using semver, calver, or custom regex patterns
- **[tagging](docs/feature-tagging.md)** - Configure version tag prefixes (e.g., v1.0.0)
- **[rules](docs/feature-rules.md)** - Extensive set of rules to enforce changelog quality and consistency

Heylogs also has some **quality-of-life** features to make it easier to use and integrate into different workflows:

- **[config file](docs/feature-config-file.md)** - Hierarchical configuration through `heylogs.properties` files, similar to Lombok's configuration system

See the [feature matrix](docs/feature-matrix.md) for a summary of feature support by usage mode.

## Cookbook

- **[badges](docs/cookbook-badges.md)** - Generate badges for unreleased changes
- **[gitflow](docs/cookbook-gitflow.md)** - Automate the changelog release
- **[tips](docs/cookbook-tips.md)** - Tips and best practices for using Heylogs effectively

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
