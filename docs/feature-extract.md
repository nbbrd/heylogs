# Extract command

The extract command filters and extracts versions from a changelog file for publication, searching, or custom output. It allows you to select specific versions, limit the number of results, and control the output format.

## Usage examples

### CLI

```bash
$ heylogs extract --limit 1 --output result.md
# Extracts the latest version and writes it to result.md
```

### Maven plugin

```xml
<plugin>
    <groupId>com.github.nbbrd.heylogs</groupId>
    <artifactId>heylogs-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>extract</goal>
            </goals>
            <configuration>
                <limit>1</limit>
                <outputFile>result.md</outputFile>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Parameters

### Changelog input parameters

| Parameter    | Description                       | CLI                      | Maven Plugin                          |
|--------------|-----------------------------------|--------------------------|---------------------------------------|
| `inputFile`  | Changelog file to extract from (default: CHANGELOG.md) | `<source>` (positional, default: CHANGELOG.md)  | `<inputFile>CHANGELOG.md</inputFile>` |

### Output parameters

| Parameter    | Description                       | CLI                      | Maven Plugin                          |
|--------------|-----------------------------------|--------------------------|---------------------------------------|
| `outputFile` | Output file for result            | `--output <file>`        | `<outputFile>result.md</outputFile>`  |

### Filter parameters

| Parameter    | Description                       | CLI                      | Maven Plugin                          |
|--------------|-----------------------------------|--------------------------|---------------------------------------|
| `ref`        | Filter versions by name           | `--ref <version>`        | `<ref>1.0.0</ref>`                    |
| `unreleasedPattern` | Pattern for unreleased versions | `--unreleased <pattern>` | `<unreleasedPattern>^.*-SNAPSHOT$</unreleasedPattern>` |
| `from`       | Filter by min date (included)     | `--from <date>`          | `<from>2026-01-01</from>`             |
| `to`         | Filter by max date (included)     | `--to <date>`            | `<to>2026-03-10</to>`                 |
| `limit`      | Limit the number of versions      | `--limit <number>`       | `<limit>1</limit>`                    |
| `ignoreContent` | Ignore versions content, keep headers only | `--ignore-content` | `<ignoreContent>true</ignoreContent>` |

---

[← Back to Features](features.md)
