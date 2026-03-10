# Push command

The push command adds a new change entry to the Unreleased section of a changelog file. It automatically creates the necessary type-of-change section (Added, Changed, Fixed, etc.) if it doesn't exist.

## Usage examples

### CLI

```bash
$ heylogs push [changelog-file] -t <type> -m "<message>"
# Add a new feature to Unreleased
heylogs push -t added -m "Add support for custom themes"
# Fix a bug
heylogs push CHANGELOG.md -t fixed -m "Fix memory leak in parser"
# Add a security fix with issue reference
heylogs push -t security -m "Fix XSS vulnerability [#456](https://github.com/user/repo/issues/456)"
# Deprecate a feature
heylogs push -t deprecated -m "Deprecate legacy API endpoints"
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

| Parameter    | Description                       | CLI                      | Maven Plugin                          |
|--------------|-----------------------------------|--------------------------|---------------------------------------|
| `type`       | Type of change (added, changed, fixed, etc.) | `-t <type>` / `--type <type>` | `<type>added</type>`                  |
| `message`    | The change message                | `-m <message>` / `--message <message>` | `<message>Some change</message>`      |

---

[← Back to README](../README.md)
