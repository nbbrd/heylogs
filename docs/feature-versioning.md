# Versioning schemes

As versions are usually not random strings, Heylogs supports several versioning schemes to validate them:

|    ID    | Description                                                                       | Example          | Argument       |
|:--------:|-----------------------------------------------------------------------------------|------------------|----------------|
| `calver` | [Calendar versioning](https://calver.org/)                                        | `2023.04.01`     | calver pattern |
| `maven`  | [Maven Versioning](https://maven.apache.org/pom.html#Version_Order_Specification) | `1.0.0-SNAPSHOT` | -              |
| `pep440` | [Python PEP 440](https://peps.python.org/pep-0440/)                               | `1.0.0a1`        | -              |
| `regex`  | Custom regex-based versioning                                                     | `X13`            | regex pattern  |
| `semver` | [Semantic versioning](https://semver.org/)                                        | `1.0.0`          | -              |

Heylogs can detect the versioning scheme automatically when scanning a changelog, 
but you need to specify it explicitly to enable validation.

## Usage examples

- `$ heylogs check -v calver:YYYY.MM.DD`  
- `$ heylogs check -v maven`  
- `$ heylogs check -v pep440`  
- `$ heylogs check -v regex:X\d+`  
- `$ heylogs check -v semver`

---

[← Back to README](../README.md)
