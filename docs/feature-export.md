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

| Parameter   | Description                                                         | CLI                                            | Maven Plugin                          |
|-------------|---------------------------------------------------------------------|------------------------------------------------|---------------------------------------|
| `inputFile` | Changelog file to export (default: CHANGELOG.md); use `-` for stdin | `<source>` (positional, default: CHANGELOG.md) | `<inputFile>CHANGELOG.md</inputFile>` |

### Output parameters

| Parameter    | Description                                                                                              | CLI               | Maven Plugin                              |
|--------------|----------------------------------------------------------------------------------------------------------|-------------------|-------------------------------------------|
| `outputFile` | Output file for structured data (default: stdout)                                                        | `--output <file>` | `<outputFile>changelog.json</outputFile>` |
| `format`     | Output format (default: auto-detected from file extension, falls back to first available content format) | `--format <id>`   | `<format>json</format>`                   |

## Output format

The JSON output is compatible with the [clparse](https://github.com/marcaddeo/clparse) format. It contains the full changelog structure including title, optional description, and all releases with their changes as a flat ordered list:

```json
{
  "title": "Changelog",
  "description": null,
  "releases": [
    {
      "version": null,
      "link": "https://github.com/owner/repo/compare/v1.0.0...HEAD",
      "date": null,
      "changes": [],
      "yanked": false
    },
    {
      "version": "1.0.0",
      "link": "https://github.com/owner/repo/releases/tag/v1.0.0",
      "date": "2024-01-01",
      "changes": [
        {
          "added": "Initial release."
        }
      ],
      "yanked": false
    }
  ]
}
```

Each entry in `changes` is a single-key object mapping the change type (`added`, `changed`, `deprecated`, `removed`, `fixed`, `security`) to the item text. Multiple items of the same type appear as separate objects in the array, preserving their original order.

---

[← Back to README](../README.md)

