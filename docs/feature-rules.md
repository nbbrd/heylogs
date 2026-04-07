# Rules

Heylogs uses a set of rules to validate and enforce changelog quality and consistency. Rules can be customized, enabled, disabled, or have their severity changed via configuration or command options.

## Rule types

- **Format rules**: Ensure changelog structure and formatting (e.g., headings, date formats).
- **Content rules**: Check for required sections, unique releases, valid links, and more.
- **Versioning rules**: Validate version numbers and tag references.

## Customization

Rules can be configured globally or per project using `heylogs.properties` or command options:

```shell
heylogs check --rule dot-space-link-style:WARN --rule no-empty-group:OFF
```

## Available rules

| Module | ID                         | Name                     | Options |
|--------|----------------------------|--------------------------|---------|
| `api`  | `all-h2-contain-a-version` | All H2 contain a version | `ERROR` |
| `api`  | `column-width`             | Column width             | `OFF`   |
| `api`  | `consistent-separator`     | Consistent separator     | `ERROR` |
| `api`  | `date-displayed`           | Date displayed           | `ERROR` |
| `api`  | `dot-space-link-style`     | Dot-space-link style     | `OFF`   |
| `api`  | `duplicate-items`          | Duplicate items          | `ERROR` |
| `api`  | `for-humans`               | For humans               | `ERROR` |
| `api`  | `forge-ref`                | Forge reference          | `ERROR` |
| `api`  | `https`                    | HTTPS                    | `ERROR` |
| `api`  | `imbalanced-braces`        | Imbalanced braces        | `ERROR` |
| `api`  | `latest-version-first`     | Latest version first     | `ERROR` |
| `api`  | `linkable`                 | Linkable                 | `ERROR` |
| `api`  | `no-empty-group`           | No empty group           | `ERROR` |
| `api`  | `no-empty-release`         | No empty release         | `ERROR` |
| `api`  | `no-link-brackets`         | No link brackets         | `ERROR` |
| `api`  | `no-orphan-ref`            | No orphan ref            | `ERROR` |
| `api`  | `release-date`             | Release date             | `WARN`  |
| `api`  | `tag-versioning`           | Tag versioning           | `ERROR` |
| `api`  | `type-of-changes-grouped`  | Type of changes grouped  | `ERROR` |
| `api`  | `unique-headings`          | Unique headings          | `ERROR` |
| `api`  | `unique-release`           | Unique release           | `ERROR` |
| `api`  | `unknown-link-type`        | Unknown link type        | `ERROR` |
| `api`  | `versioning-format`        | Versioning format        | `ERROR` |

---

[← Back to README](../README.md)
