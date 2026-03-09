# Features

Heylogs provides several **commands** to interact with changelog files:

- **[check](feature-check.md)** - Validate changelog format against an extensive set of rules
- **[scan](feature-scan.md)** - Summarize changelog content and metadata
- **[extract](feature-extract.md)** - Filter and extract specific versions
- **[release](feature-release.md)** - Convert unreleased changes into a new release
- **[list](feature-list.md)** - Display all available resources and rules

Heylogs also provides some **customizations** to adapt to various workflows:

- **[forge](feature-forge.md)** - Support for GitHub, GitLab, and Forgejo specific features
- **[versioning](feature-versioning.md)** - Validate version numbers using semver, calver, or custom regex patterns
- **[tagging](feature-tagging.md)** - Configure version tag prefixes (e.g., v1.0.0)

> [!NOTE]
> Most examples in the documentation use the CLI for the sake of simplicity.
> Command options are available through the `--help` option.

## Usage examples

Each command can be invoked through different interfaces:

- **Library**:
  ```java
  Heylogs.ofServiceLoader().check(document, Config.builder().versioningOf("semver").build())
  ```
- **Command-line interface:**:   
  ```shell 
  heylogs check --versioning semver
  ```
- **Maven plugin**:  
  ```shell 
  mvn com.github.nbbrd.heylogs:heylogs-maven-plugin::check -D heylogs.versioning=semver
  ```

## Command support matrix

| Command   | Library | CLI | Maven Plugin |
|-----------|:-------:|:---:|:------------:|
| check     |    ✔    |  ✔  |      ✔       |
| scan      |    ✔    |  ✔  |      ✔       |
| extract   |    ✔    |  ✔  |      ✔       |
| release   |    ✔    |  ✔  |      ✔       |
| list      |    ✔    |  ✔  |      ✔       |

---

[← Back to README](../README.md)

