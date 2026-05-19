# Push command

The push command adds a new change entry to the Unreleased section of a changelog file. It automatically creates the necessary type-of-change section (Added, Changed, Fixed, etc.) if it doesn't exist.

## Usage examples

### CLI

```bash
$ heylogs push [changelog-file] -y <type> -m "<message>"
# Add a new feature to Unreleased
heylogs push -y added -m "Add support for custom themes"
# Fix a bug
heylogs push CHANGELOG.md -y fixed -m "Fix memory leak in parser"
# Add a security fix with issue reference
heylogs push -y security -m "Fix XSS vulnerability [#456](https://github.com/user/repo/issues/456)"
# Deprecate a feature
heylogs push -y deprecated -m "Deprecate legacy API endpoints"
# Preview without writing the file
heylogs push -y added -m "Add support for custom themes" --dry-run
```

### Maven plugin

```xml
<plugin>
    <groupId>com.github.nbbrd.heylogs</groupId>
    <artifactId>heylogs-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>push-change</id>
            <goals>
                <goal>push</goal>
            </goals>
            <configuration>
                <type>added</type>
                <message>Add support for custom themes</message>
                <inputFile>CHANGELOG.md</inputFile>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Parameters

### Changelog input parameters

| Parameter    | Description                       | CLI                      | Maven Plugin                          |
|--------------|-----------------------------------|--------------------------|---------------------------------------|
| `inputFile`  | Changelog file to push to (default: CHANGELOG.md) | `[changelog-file]` (positional, default: CHANGELOG.md) | `<inputFile>CHANGELOG.md</inputFile>` |

### Change parameters

| Parameter | Description                                  | CLI                                    | Maven Plugin                     |
|-----------|----------------------------------------------|----------------------------------------|----------------------------------|
| `type`    | Type of change (added, changed, fixed, etc.) | `-y <type>` / `--type <type>`          | `<type>added</type>`             |
| `message` | The change message                           | `-m <message>` / `--message <message>` | `<message>Some change</message>` |
| `dryRun`  | Preview without writing the file             | `--dry-run`                            | *(not supported)*                |

## Feedback

On success the CLI prints a single line to **stderr** (message truncated to 40 characters):

```
+ [added] "Add support for custom themes" pushed into CHANGELOG.md
```

In `--dry-run` mode no file is written and the message uses the `~` prefix:

```
~ Would push [added] "Add support for custom themes" into CHANGELOG.md
```

Use `--batch` to suppress all feedback.

---

[← Back to README](../README.md)
