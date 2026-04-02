# Format command

The format command normalizes the content and enforces structural ordering in changelog files. It is idempotent: running it twice produces the same result as running it once.

The following normalizations are applied:

- **Sort type-of-change sections** into canonical order: Added, Changed, Deprecated, Removed, Fixed, Security.
- **Remove empty type-of-change groups** from released versions (empty groups in the `[Unreleased]` section are preserved as works-in-progress).
- **Sort reference links** at the bottom of the file: version references first (latest first), then remaining references alphabetically.
- **Normalize bullet markers** to `-` (replaces `*` and `+`).

## Usage examples

### CLI

```bash
# Format the default CHANGELOG.md in-place
$ heylogs format

# Format a specific file in-place
$ heylogs format CHANGELOG.md

# Check if the changelog is already properly formatted (exit code 1 if not)
$ heylogs format --check
```

**Before:**
```markdown
## [Unreleased]

### Fixed

- Fix a bug

### Added

* New feature

## [1.0.0] - 2020-01-01

### Security

- Old CVE

### Changed

- Update something

### Added

- Initial release
```

**After:**
```markdown
## [Unreleased]

### Added

- New feature

### Fixed

- Fix a bug

## [1.0.0] - 2020-01-01

### Added

- Initial release

### Changed

- Update something

### Security

- Old CVE
```

### Maven plugin

```xml
<plugin>
    <groupId>com.github.nbbrd.heylogs</groupId>
    <artifactId>heylogs-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>format</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

To enforce formatting without modifying files (e.g. in CI):

```xml
<configuration>
    <check>true</check>
</configuration>
```

### Maven Enforcer rule

```xml
<rules>
    <formatChangelog>
        <inputFiles>
            <inputFile>CHANGELOG.md</inputFile>
        </inputFiles>
        <recursive>true</recursive>
        <skip>false</skip>
    </formatChangelog>
</rules>
```

## Parameters

### Changelog input parameters

| Parameter      | Description                                                                  | CLI                                         | Maven Plugin                            | Enforcer Rule                           |
|----------------|------------------------------------------------------------------------------|---------------------------------------------|-----------------------------------------|-----------------------------------------|
| `inputFile(s)` | Changelog file(s) to format (default: CHANGELOG.md, supports multiple files) | `<file>` (positional, default: CHANGELOG.md) | `<inputFiles>CHANGELOG.md</inputFiles>` | `<inputFiles>CHANGELOG.md</inputFiles>` |
| `recursive`    | Recursively search for changelog files                                       | `-r` / `--recursive`                        | `<recursive>true</recursive>`           | `<recursive>true</recursive>`           |

### Format parameters

| Parameter | Description                                              | CLI       | Maven Plugin              | Enforcer Rule |
|-----------|----------------------------------------------------------|-----------|---------------------------|---------------|
| `check`   | Check formatting without modifying files (fails if not formatted) | `--check` | `<check>true</check>`     | *(always check mode)* |

## Notes

- The command only processes structurally valid changelogs. Files that fail basic structural validation are skipped.
- In `--check` mode (CLI) or when `check=true` (Maven plugin), the build fails with a non-zero exit code if any file requires formatting.
- The Enforcer rule always operates in check mode and fails the build if the changelog is not properly formatted.
- The `[Unreleased]` section is intentionally excluded from empty-group removal, as it represents a work in progress.

---

[← Back to README](../README.md)

