# Check command

The check command validates the format and content of changelog files against an extensive set of rules. It helps ensure changelog quality and consistency for releases and automation.

## Usage examples

### CLI

```bash
$ heylogs check
CHANGELOG.md
  No problem
```

### Maven plugin

```xml
<plugin>
    <groupId>com.github.nbbrd.heylogs</groupId>
    <artifactId>heylogs-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Maven Enforcer rule

```xml
<rules>
    <checkChangelog>
        <inputFiles>
            <inputFile>CHANGELOG.md</inputFile>
        </inputFiles>
        <recursive>true</recursive>
        <outputFile>result.txt</outputFile>
        <format>json</format>
        <versioning>semver</versioning>
        <tagging>prefix:v</tagging>
        <forge>github</forge>
        <rules>
            <rule>no-empty-group:WARN</rule>
        </rules>
        <domains>
            <domain>gitlab.company.com:gitlab</domain>
        </domains>
        <noConfig>true</noConfig>
        <skip>false</skip>
    </checkChangelog>
</rules>
```

## Parameters

### Changelog input parameters

| Parameter      | Description                                                                 | CLI                                            | Maven Plugin                            | Enforcer Rule                           |
|----------------|-----------------------------------------------------------------------------|------------------------------------------------|-----------------------------------------|-----------------------------------------|
| `inputFile(s)` | Changelog file(s) to check (default: CHANGELOG.md, supports multiple files) | `<source>` (positional, default: CHANGELOG.md) | `<inputFiles>CHANGELOG.md</inputFiles>` | `<inputFiles>CHANGELOG.md</inputFiles>` |
| `recursive`    | Recursively search for changelog files                                      | `--recursive`                                  | `<recursive>true</recursive>`           | `<recursive>true</recursive>`           |

### Output parameters

| Parameter    | Description                                         | CLI               | Maven Plugin                         | Enforcer Rule                        |
|--------------|-----------------------------------------------------|-------------------|--------------------------------------|--------------------------------------|
| `outputFile` | Output file for result                              | `--output <file>` | `<outputFile>result.md</outputFile>` | `<outputFile>result.md</outputFile>` |
| `format`     | Output format (stylish, json, github-actions, etc.) | `--format <id>`   | `<format>json</format>`              | `<format>json</format>`              |

### Configuration options

| Parameter    | Description                       | CLI                       | Maven Plugin                                   | Enforcer Rule                                  |
|--------------|-----------------------------------|---------------------------|------------------------------------------------|------------------------------------------------|
| `noConfig`   | Ignore config files               | `--no-config`             | `<noConfig>true</noConfig>`                    | `<noConfig>true</noConfig>`                    |
| `versioning` | Versioning scheme                 | `--versioning <scheme>`   | `<versioning>semver</versioning>`              | `<versioning>semver</versioning>`              |
| `tagging`    | Tagging strategy                  | `--tagging <strategy>`    | `<tagging>prefix:v</tagging>`                  | `<tagging>prefix:v</tagging>`                  |
| `forge`      | Forge platform                    | `--forge <platform>`      | `<forge>github</forge>`                        | `<forge>github</forge>`                        |
| `rules`      | Rule overrides (comma-separated)  | `--rule <id:severity>`    | `<rules>no-empty-group:WARN,...</rules>`       | `<rules>no-empty-group:WARN,...</rules>`       |
| `domains`    | Domain mappings (comma-separated) | `--domain <domain:forge>` | `<domains>gitlab.company.com:gitlab</domains>` | `<domains>gitlab.company.com:gitlab</domains>` |

> [!TIP]
> Rule configuration can be modified using the `--rule` option.
> For example, upgrading the severity of the `dot-space-link-style` rule from `OFF` to `WARN` and disabling the `no-empty-group` rule:
> `$ heylogs check --rule dot-space-link-style:WARN --rule no-empty-group:OFF`

## Configuration

The check command supports hierarchical configuration via `heylogs.properties` files. You can specify versioning, tagging, forge, and rule options globally or per module.

---

[← Back to README](../README.md)
