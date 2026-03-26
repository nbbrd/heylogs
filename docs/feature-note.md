# Note command

The note command sets or updates the summary (abstract) text immediately after the Unreleased header in a changelog file. This summary provides context or highlights for the upcoming release and is useful for communicating important information to users.

## Usage examples

### CLI

```bash
# Set the summary for the Unreleased section in the default CHANGELOG.md
$ heylogs note -m "This release introduces major improvements to performance."

# Set the summary in a specific changelog file
$ heylogs note custom-changelog.md -m "Security fixes and dependency updates."
```

### Maven plugin

```xml
<plugin>
    <groupId>com.github.nbbrd.heylogs</groupId>
    <artifactId>heylogs-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>note</goal>
            </goals>
            <configuration>
                <message>This release introduces major improvements to performance.</message>
                <inputFile>CHANGELOG.md</inputFile>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Parameters

### Changelog input parameters

| Parameter    | Description                                 | CLI                                 | Maven Plugin                          |
|--------------|---------------------------------------------|-------------------------------------|---------------------------------------|
| `inputFile`  | Changelog file to update (default: CHANGELOG.md) | `[changelog-file]` (positional, default: CHANGELOG.md) | `<inputFile>CHANGELOG.md</inputFile>` |

### Note parameters

| Parameter | Description           | CLI                                 | Maven Plugin                          |
|-----------|----------------------|-------------------------------------|---------------------------------------|
| `message` | The summary text     | `-m <message>` / `--message <message>` | `<message>Some summary</message>`     |

## Notes

- The summary is placed immediately after the `## [Unreleased]` header.
- If a summary already exists, it will be replaced by the new message.
- The command fails if the changelog file does not exist.

---

[← Back to README](../README.md)

