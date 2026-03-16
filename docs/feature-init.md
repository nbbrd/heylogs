# Init command

The init command creates a new changelog file following the [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) format. It generates a minimal template with a header and an empty Unreleased section. The command fails if the file already exists (CLI) or silently skips it (Maven plugin).

## Usage examples

### CLI

```bash
# Create CHANGELOG.md in the current directory
$ heylogs init

# Create a changelog at a custom path
$ heylogs init path/to/CHANGELOG.md

# Create a changelog with a versioning reference in the description
$ heylogs init --versioning semver

# Create a changelog from a custom Mustache template
$ heylogs init --template my-template.mustache
```

### Maven plugin

```xml
<plugin>
    <groupId>com.github.nbbrd.heylogs</groupId>
    <artifactId>heylogs-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>init</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## Parameters

### Output parameters

| Parameter    | Description                                            | CLI                     | Maven Plugin                              |
|--------------|--------------------------------------------------------|-------------------------|-------------------------------------------|
| `outputFile` | Changelog file to create (default: CHANGELOG.md)       | `<file>` (positional)   | `<outputFile>CHANGELOG.md</outputFile>`   |
| `template`   | Custom [Mustache](https://mustache.github.io/) template file | `--template <file>` | `<template>my-template.mustache</template>` |

### Configuration options

| Parameter    | Description                       | CLI                      | Maven Plugin                                    |
|--------------|-----------------------------------|--------------------------|-------------------------------------------------|
| `noConfig`   | Ignore config files               | `--no-config`            | `<noConfig>true</noConfig>`                     |
| `versioning` | Versioning scheme (used in description line) | `--versioning <scheme>` | `<versioning>semver</versioning>`      |
| `tagging`    | Tagging strategy                  | `--tagging <strategy>`   | `<tagging>prefix:v</tagging>`                   |
| `forge`      | Forge platform                    | `--forge <platform>`     | `<forge>github</forge>`                         |
| `rules`      | Rule overrides (comma-separated)  | `--rule <id:severity>`   | `<rules>no-empty-group:WARN,...</rules>`         |
| `domains`    | Domain mappings (comma-separated) | `--domain <domain:forge>`| `<domains>gitlab.company.com:gitlab</domains>`  |

## Template

The built-in template produces a changelog in this form (without `--versioning`):

```markdown
# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]
```

When `--versioning semver` is specified, the description line is extended:

```markdown
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/).
```

### Custom template

The `--template` option accepts a [Mustache](https://mustache.github.io/) template file. The following variables are available:

#### `versioning` (object, present only when `--versioning` is set)

| Variable              | Description                                         |
|-----------------------|-----------------------------------------------------|
| `{{versioning.id}}`   | Versioning scheme ID (e.g. `semver`, `calver`)      |
| `{{versioning.arg}}`  | Versioning argument, if any (e.g. `YYYY.MM`)        |
| `{{versioning.name}}` | Human-readable name (e.g. `Semantic Versioning`)    |
| `{{versioning.url}}`  | URL of the versioning specification                 |

#### `tagging` (object, present only when `--tagging` is set)

| Variable           | Description                                            |
|--------------------|--------------------------------------------------------|
| `{{tagging.id}}`   | Tagging strategy ID (e.g. `prefix`)                   |
| `{{tagging.arg}}`  | Tagging argument, if any (e.g. `v`)                   |

#### `forge` (object, present only when `--forge` is set)

| Variable        | Description                                               |
|-----------------|-----------------------------------------------------------|
| `{{forge.id}}`  | Forge platform ID (e.g. `github`, `gitlab`, `forgejo`)   |

#### `rules` (list, always present, empty when no rules are configured)

| Variable            | Description                                          |
|---------------------|------------------------------------------------------|
| `{{rules.id}}`      | Rule ID (e.g. `linkable`)                            |
| `{{rules.severity}}`| Severity override (e.g. `WARN`, `ERROR`, `OFF`)      |

#### `domains` (list, always present, empty when no domains are configured)

| Variable              | Description                                        |
|-----------------------|----------------------------------------------------|
| `{{domains.domain}}`  | Domain name (e.g. `gitlab.company.com`)            |
| `{{domains.forgeId}}` | Forge ID mapped to this domain (e.g. `gitlab`)     |

Example custom template:

```mustache
# Changelog{{#versioning}} — {{name}}{{/versioning}}

> All notable changes to this project will be documented in this file.
{{#forge}}
> Hosted on {{id}}.
{{/forge}}

## [Unreleased]
```

---

[← Back to README](../README.md)

