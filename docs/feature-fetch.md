# Fetch command

The fetch command retrieves the title of a forge issue or pull request and adds it as a new change entry in the Unreleased section of a changelog file. The id can be provided either as a full URL or as a short ref (e.g. `#1`); in the latter case, the forge URL is inferred from the existing compare link in the changelog.

## Usage examples

### CLI

```bash
# Fetch an issue by full URL and add it as an "Added" change
$ heylogs fetch -y added -i https://github.com/nbbrd/heylogs/issues/1

# Fetch a pull request by full URL and add it as a "Fixed" change
$ heylogs fetch -y fixed -i https://github.com/nbbrd/heylogs/pull/42

# Fetch an issue by short ref (forge inferred from the changelog compare link)
$ heylogs fetch CHANGELOG.md -y added -i "#1"
```

### Maven plugin

```xml
<plugin>
    <groupId>com.github.nbbrd.heylogs</groupId>
    <artifactId>heylogs-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>fetch-change</id>
            <goals>
                <goal>fetch</goal>
            </goals>
            <configuration>
                <type>added</type>
                <id>https://github.com/nbbrd/heylogs/issues/1</id>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Parameters

### Changelog input parameters

| Parameter   | Description                                      | CLI                     | Maven Plugin                          |
|-------------|--------------------------------------------------|-------------------------|---------------------------------------|
| `inputFile` | Changelog file to update (default: CHANGELOG.md) | `<source>` (positional) | `<inputFile>CHANGELOG.md</inputFile>` |

### Fetch parameters

| Parameter | Description                                                           | CLI                           | Maven Plugin         |
|-----------|-----------------------------------------------------------------------|-------------------------------|----------------------|
| `type`    | Type of change (added, changed, deprecated, removed, fixed, security) | `-y <type>` / `--type <type>` | `<type>added</type>` |
| `id`      | Full URL or short ref of the forge issue or pull request              | `-i <id>` / `--id <id>`       | `<id>https://…</id>` |

### Configuration options

| Parameter    | Description                       | CLI                       | Maven Plugin                                   |
|--------------|-----------------------------------|---------------------------|------------------------------------------------|
| `noConfig`   | Ignore config files               | `--no-config`             | `<noConfig>true</noConfig>`                    |
| `versioning` | Versioning scheme                 | `--versioning <scheme>`   | `<versioning>semver</versioning>`              |
| `tagging`    | Tagging strategy                  | `--tagging <strategy>`    | `<tagging>prefix:v</tagging>`                  |
| `forge`      | Forge platform                    | `--forge <platform>`      | `<forge>github</forge>`                        |
| `rules`      | Rule overrides (comma-separated)  | `--rule <id:severity>`    | `<rules>no-empty-group:WARN,...</rules>`       |
| `domains`    | Domain mappings (comma-separated) | `--domain <domain:forge>` | `<domains>gitlab.company.com:gitlab</domains>` |

---

[← Back to README](../README.md)
