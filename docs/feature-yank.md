# Yank command

The yank command marks an existing release as yanked by appending the `[YANKED]` label to its heading. A yanked release is one that has been pulled back — typically because it introduced a critical bug or security issue — and should not be used.

## Usage examples

### CLI

```bash
# Mark release 1.0.0 as yanked in the default CHANGELOG.md
$ heylogs yank -r 1.0.0

# Mark release 1.0.0 as yanked in a specific changelog file
$ heylogs yank CHANGELOG.md -r 1.0.0
```

**Before:**
```markdown
## [1.0.0] - 2020-01-01
```

**After:**
```markdown
## [1.0.0] - 2020-01-01 [YANKED]
```

### Maven plugin

```xml
<plugin>
    <groupId>com.github.nbbrd.heylogs</groupId>
    <artifactId>heylogs-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>yank</goal>
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

| Parameter   | Description                                      | CLI                     | Maven Plugin                          |
|-------------|--------------------------------------------------|-------------------------|---------------------------------------|
| `inputFile` | Changelog file to update (default: CHANGELOG.md) | `<source>` (positional) | `<inputFile>CHANGELOG.md</inputFile>` |

### Yank parameters

| Parameter | Description               | CLI                        | Maven Plugin       |
|-----------|---------------------------|----------------------------|--------------------|
| `ref`     | Version reference to yank | `-r <ref>` / `--ref <ref>` | `<ref>1.0.0</ref>` |

## Notes

- The command fails if the specified version does not exist in the changelog.
- The command fails if the version is already marked as yanked.
- The `[Unreleased]` section cannot be yanked.
- The reference link definition (e.g. `[1.0.0]: https://...`) is not modified by this command.

---

[← Back to README](../README.md)

