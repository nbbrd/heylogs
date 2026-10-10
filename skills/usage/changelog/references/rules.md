# Heylogs built-in rules

Rules fall into three kinds: **format** (structure/formatting), **content**
(required sections, links, uniqueness), and **versioning** (version numbers and
tag refs). The table below lists every built-in rule and its **default**
severity. The reference description for each is in
[`docs/feature-rules.md`](../../../docs/feature-rules.md).

| ID                         | Default | What it enforces                                                                 |
|----------------------------|---------|----------------------------------------------------------------------------------|
| `all-h2-contain-a-version` | ERROR   | Every `##` heading parses as `[ref] - YYYY-MM-DD` or `[Unreleased]`.            |
| `date-displayed`           | ERROR   | Every released version heading has a `YYYY-MM-DD` date (Unreleased exempt).      |
| `for-humans`               | ERROR   | Exactly one `# Changelog` heading.                                               |
| `latest-version-first`     | ERROR   | Released versions in reverse-chronological order; Unreleased on top.            |
| `linkable`                 | ERROR   | Every version heading is linkable (inline or reference-style with a definition). |
| `type-of-changes-grouped`  | ERROR   | Every `###` heading is one of Added/Changed/Deprecated/Removed/Fixed/Security.   |
| `column-width`             | OFF     | Bullet lines ≤ 80 chars. Enable for a hard line-length policy.                   |
| `consistent-separator`     | ERROR   | One ref↔date separator style per file (`-`, `–`, or `—`).                        |
| `dot-space-link-style`     | OFF     | Trailing forge links preceded by `. ` (e.g. `Fix typo. [#42](…)`).               |
| `duplicate-items`          | ERROR   | No two identical bullet items within the same group.                            |
| `forge-ref`                | ERROR   | Forge links use the canonical URL format for the detected forge.                |
| `https`                    | ERROR   | `http://` links must be `https://`.                                              |
| `imbalanced-braces`        | ERROR   | No unmatched `{`/`}` in bullet text (outside code spans).                        |
| `no-empty-group`           | ERROR   | A `###` group must have at least one bullet item.                               |
| `no-empty-release`         | ERROR   | A released version must contain at least one item.                              |
| `no-link-brackets`         | ERROR   | Forge reference links not wrapped in `()` or `[]`.                              |
| `no-orphan-ref`            | ERROR   | Every reference-link definition is used at least once.                          |
| `no-version-regression`    | ERROR   | Version numbers don't decrease top-to-bottom (when a scheme is active).          |
| `release-date`             | WARN    | Release dates are not in the future.                                            |
| `tag-versioning`           | ERROR   | Version refs match the configured tag scheme (e.g. `v1.0.0`).                    |
| `unique-headings`          | ERROR   | No two version headings have identical text.                                    |
| `unique-release`           | ERROR   | No two versions share the same `ref`.                                            |
| `unknown-link-type`        | WARN    | Forge-matching reference defs whose link type can't be recognised.              |
| `versioning-format`        | ERROR   | Version refs conform to the active scheme (SemVer/CalVer/custom).               |

Discover the rules installed in your environment (including extensions) with:

```bash
heylogs list                 # human-readable; also lists formats, forges, schemes
heylogs list --format json
```

Override any rule's severity ad hoc with `--rule <id>:<severity>` (`ERROR`,
`WARN`, `INFO`, `OFF`), or persistently via the `rules` list in
`heylogs.properties` — see [`config-file.md`](config-file.md).
