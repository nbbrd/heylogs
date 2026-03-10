# List command

The list command displays all resources available in the application, including supported forges, formats, rules, tagging, and versioning schemes. It is useful for discovering what features and options are available.

## Usage examples

### CLI

```bash
$ heylogs list --output resources.txt --format json
# Lists all resources in JSON format and writes to resources.txt
```

### Maven plugin

```xml
<plugin>
    <groupId>com.github.nbbrd.heylogs</groupId>
    <artifactId>heylogs-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>list</goal>
            </goals>
            <configuration>
                <outputFile>resources.txt</outputFile>
                <format>json</format>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Parameters

### Output parameters
| Parameter    | Description                       | CLI                      | Maven Plugin                          |
|--------------|-----------------------------------|--------------------------|---------------------------------------|
| `outputFile` | Output file for result            | `--output <file>`        | `<outputFile>resources.txt</outputFile>`  |
| `format`     | Output format (stylish, json)     | `--format <id>`          | `<format>json</format>`               |

---

[← Back to README](../README.md)
