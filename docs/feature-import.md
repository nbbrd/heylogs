# Import command

The import command converts structured data (e.g. JSON) back into a Markdown changelog file. This is the inverse of the [export](feature-export.md) command and is useful for reconstructing a changelog from a machine-readable representation, or for applying programmatic modifications to changelog content.

## Usage examples

### CLI

```bash
# Import a JSON file and write the result to CHANGELOG.md (default output)
$ heylogs import changelog.json
# Import a JSON file and write to a specific output file
$ heylogs import changelog.json --output result.md
# Preview what would be written without actually writing (dry-run)
$ heylogs import changelog.json --dry-run
# Round-trip: export then import
$ heylogs export --output changelog.json && heylogs import changelog.json
```

### Maven plugin

```xml
<plugin>
    <groupId>com.github.nbbrd.heylogs</groupId>
    <artifactId>heylogs-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>import</goal>
            </goals>
            <configuration>
                <inputFile>changelog.json</inputFile>
                <outputFile>CHANGELOG.md</outputFile>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Parameters

### Input parameters

| Parameter   | Description                                                                                             | CLI                               | Maven Plugin                            |
|-------------|---------------------------------------------------------------------------------------------------------|-----------------------------------|-----------------------------------------|
| `inputFile` | Structured data file to import (required); format inferred from file extension                          | `<source>` (positional, required) | `<inputFile>changelog.json</inputFile>` |
| `format`    | Input format (default: auto-detected from file extension, falls back to first available content format) | `--format <id>`                   | `<format>json</format>`                 |

### Output parameters

| Parameter    | Description                                  | CLI               | Maven Plugin                            |
|--------------|----------------------------------------------|-------------------|-----------------------------------------|
| `outputFile` | Output Markdown file (default: CHANGELOG.md) | `--output <file>` | `<outputFile>CHANGELOG.md</outputFile>` |

### Dry-run

| Parameter | Description                                 | CLI         | Maven Plugin      |
|-----------|---------------------------------------------|-------------|-------------------|
| `dryRun`  | Preview the import without writing any file | `--dry-run` | *(not supported)* |

## Input format

The expected JSON format is compatible with [clparse](https://github.com/marcaddeo/clparse) and mirrors the [export output](feature-export.md#output-format). Each release uses a flat `changes` array where every entry is a single-key object:
```json
{
  "title": "Changelog",
  "description": "All notable changes.",
  "releases": [
    {
      "version": null,
      "link": "https://github.com/owner/repo/compare/v1.0.0...HEAD",
      "date": null,
      "changes": [
        { "added": "First feature" },
        { "fixed": "Fix bug" }
      ],
      "yanked": false
    }
  ]
}
```

## Feedback

On success, the command prints a confirmation to **stderr**:
```
+ Imported changelog.json -> CHANGELOG.md
```

In dry-run mode it prints instead:
```
~ Would import changelog.json -> CHANGELOG.md
```

---

[← Back to README](../README.md)
