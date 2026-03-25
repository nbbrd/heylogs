# Release command

The release command finalizes unreleased changes in a changelog file by creating a new release entry. This is useful for marking a new version and preparing your changelog for publication or deployment.

## Usage examples

### CLI

```bash
$ heylogs release --ref 1.0.0
# Converts Unreleased changes into a new release with version 1.0.0
```

### Maven Plugin

```xml
<plugin>
    <groupId>com.github.nbbrd.heylogs</groupId>
    <artifactId>heylogs-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>release</goal>
            </goals>
            <configuration>
                <ref>1.0.0</ref>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Parameters

### Changelog input parameters

| Parameter   | Description                                       | CLI                     | Maven Plugin                          |
|-------------|---------------------------------------------------|-------------------------|---------------------------------------|
| `inputFile` | Changelog file to release (default: CHANGELOG.md) | `<source>` (positional) | `<inputFile>CHANGELOG.md</inputFile>` |

### Release parameters

| Parameter    | Description                       | CLI                      | Maven Plugin                          |
|--------------|-----------------------------------|--------------------------|---------------------------------------|
| `ref`        | New release version to create     | `--ref <version>`        | `<ref>1.0.0</ref>`                    |
| `date`       | Release date (default: today)     | `--date <date>`          | `<date>2026-03-10</date>`             |

### Configuration options

| Parameter    | Description                       | CLI                       | Maven Plugin                                   |
|--------------|-----------------------------------|---------------------------|------------------------------------------------|
| `noConfig`   | Ignore config files               | `--no-config`             | `<noConfig>true</noConfig>`                    |
| `versioning` | Versioning scheme                 | `--versioning <scheme>`   | `<versioning>semver</versioning>`              |
| `tagging`    | Tagging strategy                  | `--tagging <strategy>`    | `<tagging>prefix:v</tagging>`                  |
| `forge`      | Forge platform                    | `--forge <platform>`      | `<forge>github</forge>`                        |
| `rules`      | Rule overrides (comma-separated)  | `--rule <id:severity>`    | `<rules>no-empty-group:WARN,...</rules>`       |
| `domains`    | Domain mappings (comma-separated) | `--domain <domain:forge>` | `<domains>gitlab.company.com:gitlab</domains>` |

## Configuration

The release command supports hierarchical configuration via `heylogs.properties` files. You can specify versioning, tagging, and forge options globally or per module.

---

[← Back to README](../README.md)
