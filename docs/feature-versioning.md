# Versioning schemes

As versions are usually not random strings, Heylogs supports several versioning schemes to validate them:

|    ID    | Description                                | Example      | Argument       |
|:--------:|--------------------------------------------|--------------|----------------|
| `semver` | [Semantic versioning](https://semver.org/) | `1.0.0`      | -              |
| `calver` | [Calendar versioning](https://calver.org/) | `2023.04.01` | calver pattern |
| `regex`  | Custom regex-based versioning              | `X13`        | regex pattern  |

Heylogs can detect the versioning scheme automatically when scanning a changelog, 
but you need to specify it explicitly to enable validation.

## Usage examples

- `$ heylogs check -v semver`  
- `$ heylogs check -v calver:YYYY.MM.DD`  
- `$ heylogs check -v regex:X\d+`

---

[← Back to README](../README.md)
