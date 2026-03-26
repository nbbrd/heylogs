# Feature Matrix

This matrix shows which Heylogs features are supported by each usage mode.

| Feature                               | CLI | Maven Plugin | Enforcer Rules | Library | CI/CD |
|---------------------------------------|:---:|:------------:|:--------------:|:-------:|:-----:|
| [Init](feature-init.md)               |  ✔  |      ✔       |       ✖        |    ✔    |   ✔   |
| [Check](feature-check.md)             |  ✔  |      ✔       |       ✔        |    ✔    |   ✔   |
| [Scan](feature-scan.md)               |  ✔  |      ✔       |       ✖        |    ✔    |   ✔   |
| [List](feature-list.md)               |  ✔  |      ✔       |       ✖        |    ✔    |   ✔   |
| [Extract](feature-extract.md)         |  ✔  |      ✔       |       ✖        |    ✔    |   ✔   |
| [Push](feature-push.md)               |  ✔  |      ✔       |       ✖        |    ✔    |   ✔   |
| [Fetch](feature-fetch.md)             |  ✔  |      ✔       |       ✖        |    ✔    |   ✔   |
| [Note](feature-note.md)               |  ✔  |      ✔       |       ✖        |    ✔    |   ✔   |
| [Release](feature-release.md)         |  ✔  |      ✔       |       ✖        |    ✔    |   ✔   |
| [Yank](feature-yank.md)               |  ✔  |      ✔       |       ✖        |    ✔    |   ✔   |
| [Forge](feature-forge.md)             |  ✔  |      ✔       |       ✔        |    ✔    |   ✔   |
| [Versioning](feature-versioning.md)   |  ✔  |      ✔       |       ✔        |    ✔    |   ✔   |
| [Tagging](feature-tagging.md)         |  ✔  |      ✔       |       ✔        |    ✔    |   ✔   |
| [Rules](feature-rules.md)             |  ✔  |      ✔       |       ✔        |    ✔    |   ✔   |
| [Config File](feature-config-file.md) |  ✔  |      ✔       |       ✔        |    ✔    |   ✔   |

**Legend:**
- ✔ = Supported
- ✖ = Not supported

**Usage Modes:**
- **CLI**: [Command-line interface](usage-cli.md)
- **Maven Plugin**: [Maven build integration](usage-maven-plugin.md)
- **Enforcer Rules**: [Maven Enforcer rules](usage-enforcer-rules.md)
- **Library**: [Java API](usage-library.md)
- **CI/CD**: [Automation via CLI, Maven, or JBang in pipelines](usage-pipelines.md)

See each feature's documentation for details and limitations.
