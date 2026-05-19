# Note command

The note command sets or updates the summary (abstract) text immediately after the Unreleased header in a changelog file. This summary provides context or highlights for the upcoming release and is useful for communicating important information to users.

## Usage examples

### CLI

```bash
# Set the summary for the Unreleased section in the default CHANGELOG.md
$ heylogs note -m "This release introduces major improvements to performance."

# Set the summary in a specific changelog file
$ heylogs note custom-changelog.md -m "Security fixes and dependency updates."

# Preview without writing the file
$ heylogs note -m "This release introduces major improvements." --dry-run
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
| `dryRun`  | Preview without writing the file | `--dry-run`            | *(not supported)*                     |

## Notes

- The summary is placed immediately after the `## [Unreleased]` header.
- If a summary already exists, it will be replaced by the new message.
- The command fails if the changelog file does not exist.
- Messages longer than 40 characters are truncated in the feedback line (the full message is always written to the file).

## Feedback

On success the CLI prints a single line to **stderr** (message truncated to 40 characters):

```
+ Note set "This release introduces major imp..." in CHANGELOG.md
```

In `--dry-run` mode no file is written and the message uses the `~` prefix:

```
~ Would set note "This release introduces major imp..." in CHANGELOG.md
```

Use `--batch` to suppress all feedback.

---

[← Back to README](../README.md)

