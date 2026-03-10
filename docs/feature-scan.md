# Scan command

The scan command summarizes the content of changelog files to provide a quick overview of a repository. It reports release count, date range, versioning compatibility, forge information, and unreleased changes.

## Usage examples

### CLI

```bash
$ heylogs scan --output summary.txt --format json
# Summarizes changelog content in JSON format and writes to summary.txt
```

### Maven plugin

```xml
<plugin>
    <groupId>com.github.nbbrd.heylogs</groupId>
    <artifactId>heylogs-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>scan</goal>
            </goals>
            <configuration>
                <outputFile>summary.txt</outputFile>
                <format>json</format>
                <inputFiles>
                    <inputFile>CHANGELOG.md</inputFile>
                </inputFiles>
                <recursive>true</recursive>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Parameters

### Changelog input parameters

| Parameter    | Description                       | CLI                      | Maven Plugin                          |
|--------------|-----------------------------------|--------------------------|---------------------------------------|
| `inputFile(s)`| Changelog file(s) to scan (default: CHANGELOG.md, supports multiple files) | `<source>` (positional, default: CHANGELOG.md) | `<inputFiles>CHANGELOG.md</inputFiles>` |
| `recursive`  | Recursively search for changelog files | `--recursive`           | `<recursive>true</recursive>`         |

### Output parameters

| Parameter    | Description                       | CLI                      | Maven Plugin                          |
|--------------|-----------------------------------|--------------------------|---------------------------------------|
| `outputFile` | Output file for result            | `--output <file>`        | `<outputFile>summary.txt</outputFile>`  |
| `format`     | Output format (stylish, json)     | `--format <id>`          | `<format>json</format>`               |

---

[← Back to Features](features.md)
