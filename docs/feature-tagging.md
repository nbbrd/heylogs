# Tagging Strategy

Versions and release tags are often linked for convenience.
The most common convention is to use the version with a `v` prefix (i.e. version `1.0.0` is tagged as `v1.0.0`).

Heylogs supports the following tagging strategies:

|    ID    | Description               | Example | Argument         |
|:--------:|---------------------------|---------|------------------|
| `prefix` | Tags with a common prefix | `v1.0`  | non-empty string |

## Examples

- `$ heylogs check -t prefix:v`

---

[← Back to Features](features.md)

