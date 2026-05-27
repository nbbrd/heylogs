# Export command

The export command converts a changelog file into structured data (e.g. JSON). This is useful for integrating changelog information into other tools, dashboards, or automation pipelines.

The command reads a Markdown changelog (or version sections piped from `extract`) and serializes it into the requested structured format.

## Usage examples

### CLI

```bash
# Export the default CHANGELOG.md to JSON on stdout
$ heylogs export --format json

# Export to a JSON file (format inferred from extension)
$ heylogs export --output changelog.json

# Export a specific changelog file
$ heylogs export path/to/CHANGELOG.md --output changelog.json

# Pipe with extract: export only the latest version as JSON
$ heylogs extract --limit 1 | heylogs export - --format json
```

### Maven plugin

```xml
<plugin>
    <groupId>com.github.nbbrd.heylogs</groupId>
    <artifactId>heylogs-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>export</goal>
            </goals>
            <configuration>
                <outputFile>changelog.json</outputFile>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Parameters

### Changelog input parameters

| Parameter   | Description                                                    | CLI                                            | Maven Plugin                          |
|-------------|----------------------------------------------------------------|------------------------------------------------|---------------------------------------|
| `inputFile` | Changelog file to export (default: CHANGELOG.md); use `-` for stdin | `<source>` (positional, default: CHANGELOG.md) | `<inputFile>CHANGELOG.md</inputFile>` |

### Output parameters

| Parameter    | Description                                        | CLI               | Maven Plugin                          |
|--------------|----------------------------------------------------|-------------------|---------------------------------------|
| `outputFile` | Output file for structured data (default: stdout) | `--output <file>` | `<outputFile>changelog.json</outputFile>` |
| `format`     | Output format (default: auto-detected from file extension, falls back to first available content format) | `--format <id>` | `<format>json</format>` |

## Output format

The JSON output contains the full changelog structure including title, optional description, and all versions with their changes grouped by type:

```json
{
  "title": "Changelog",
  "description": null,
  "versions": [
    {
      "version": "Unreleased",
      "date": null,
      "yanked": false,
      "link": null,
      "changes": {
        "added": [],
        "changed": [],
        "deprecated": [],
        "removed": [],
        "fixed": [],
        "security": []
      }
    },
    {
      "version": "1.0.0",
      "date": "2024-01-01",
      "yanked": false,
      "link": "https://github.com/owner/repo/releases/tag/v1.0.0",
      "changes": {
        "added": ["Initial release."],
        "changed": [],
        "deprecated": [],
        "removed": [],
        "fixed": [],
        "security": []
      }
    }
  ]
}
```

---

[← Back to README](../README.md)

