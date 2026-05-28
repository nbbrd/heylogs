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

| Module  | ID                                                      | Name                     | Options |
|---------|---------------------------------------------------------|--------------------------|---------|
| `api`   | [`all-h2-contain-a-version`](#all-h2-contain-a-version) | All H2 contain a version | `ERROR` |
| `api`   | [`date-displayed`](#date-displayed)                     | Date displayed           | `ERROR` |
| `api`   | [`for-humans`](#for-humans)                             | For humans               | `ERROR` |
| `api`   | [`latest-version-first`](#latest-version-first)         | Latest version first     | `ERROR` |
| `api`   | [`linkable`](#linkable)                                 | Linkable                 | `ERROR` |
| `api`   | [`type-of-changes-grouped`](#type-of-changes-grouped)   | Type of changes grouped  | `ERROR` |
| `rules` | [`column-width`](#column-width)                         | Column width             | `OFF`   |
| `rules` | [`consistent-separator`](#consistent-separator)         | Consistent separator     | `ERROR` |
| `rules` | [`dot-space-link-style`](#dot-space-link-style)         | Dot-space-link style     | `OFF`   |
| `rules` | [`duplicate-items`](#duplicate-items)                   | Duplicate items          | `ERROR` |
| `rules` | [`forge-ref`](#forge-ref)                               | Forge reference          | `ERROR` |
| `rules` | [`https`](#https)                                       | HTTPS                    | `ERROR` |
| `rules` | [`imbalanced-braces`](#imbalanced-braces)               | Imbalanced braces        | `ERROR` |
| `rules` | [`no-empty-group`](#no-empty-group)                     | No empty group           | `ERROR` |
| `rules` | [`no-empty-release`](#no-empty-release)                 | No empty release         | `ERROR` |
| `rules` | [`no-link-brackets`](#no-link-brackets)                 | No link brackets         | `ERROR` |
| `rules` | [`no-orphan-ref`](#no-orphan-ref)                       | No orphan ref            | `ERROR` |
| `rules` | [`no-version-regression`](#no-version-regression)       | No version regression    | `ERROR` |
| `rules` | [`release-date`](#release-date)                         | Release date             | `WARN`  |
| `rules` | [`tag-versioning`](#tag-versioning)                     | Tag versioning           | `ERROR` |
| `rules` | [`unique-headings`](#unique-headings)                   | Unique headings          | `ERROR` |
| `rules` | [`unique-release`](#unique-release)                     | Unique release           | `ERROR` |
| `rules` | [`unknown-link-type`](#unknown-link-type)               | Unknown link type        | `WARN`  |
| `rules` | [`versioning-format`](#versioning-format)               | Versioning format        | `ERROR` |

---

## Rule reference

### `all-h2-contain-a-version`
Every H2 heading (`##`) must parse as a valid version entry in the form `[ref] - YYYY-MM-DD` or `[Unreleased]`. Headings that do not conform are flagged so that non-version H2 content cannot silently appear in the changelog.

### `column-width`
Bullet item lines must not exceed 80 characters. Disabled by default (`OFF`); enable it when your team enforces a hard line-length policy.

### `consistent-separator`
All version headings must use the same character to separate the version ref from the date. Accepts hyphen (`-`), en-dash (`–`), and em-dash (`—`), but only one style per file.

### `date-displayed`
Every released version heading must include a date in `YYYY-MM-DD` format. The `[Unreleased]` entry is exempt. Implements the Keep a Changelog principle *"The release date of each version is displayed."*

### `dot-space-link-style`
Forge issue or pull-request links that appear at the end of a bullet item must be preceded by `. ` (period then space), e.g., `Fix typo. [#42](…)`. Disabled by default (`OFF`).

### `duplicate-items`
No two bullet items within the same type-of-change group may have identical text. Catches copy-paste mistakes when the same entry is recorded more than once.

### `for-humans`
The document must contain exactly one `# Changelog` heading with the text `Changelog`. Implements the Keep a Changelog principle *"Made for humans, not machines."*

### `forge-ref`
Forge-hosted links (GitHub, GitLab, Forgejo, …) embedded in bullet items must use the canonical URL format for the detected forge. Prevents broken or ambiguous cross-references.

### `https`
All `http://` links must be upgraded to `https://`. Non-HTTP schemes (`mailto:`, relative paths, anchors) are ignored.

### `imbalanced-braces`
Bullet item text must not contain unmatched `{` or `}` outside of code spans. Catches template placeholders or formatting errors left in the text.

### `latest-version-first`
Released versions must appear in reverse-chronological order (most recent date first). `[Unreleased]` is always expected at the top.

### `linkable`
Every version heading must be linkable — either as an inline link `[1.0.0](url)` or a reference-style link `[1.0.0]` with a matching definition at the bottom of the file. Enables per-version deep links in rendered changelogs.

### `no-empty-group`
A type-of-change heading (`### Added`, `### Fixed`, …) must not appear without at least one bullet item beneath it. Remove the heading instead of leaving it empty.

### `no-empty-release`
A released version section must contain at least one item in at least one type-of-change group. An empty release entry provides no useful information to readers.

### `no-link-brackets`
Forge reference links must not be surrounded by parentheses or square brackets (e.g., `([#1](url))` is flagged). The link itself should appear directly in the text.

### `no-orphan-ref`
Every reference link definition at the bottom of the file (e.g., `[1.0.0]: https://…`) must be referenced at least once in the document body. Orphaned definitions are likely leftover from editing.

### `no-version-regression`
When a versioning scheme is active, version numbers must not decrease as you move from top to bottom in the file. Detects accidental ordering mistakes when entries are not purely date-sorted.

### `release-date`
Release dates must not be set in the future. Warns (`WARN`) when a version heading carries a date later than today (UTC), which typically indicates a pre-publication mistake.

### `tag-versioning`
Version refs must conform to the configured tag naming scheme (e.g., `v1.0.0` vs `1.0.0`). Ensures changelog refs match the VCS tags that will be created on release.

### `type-of-changes-grouped`
Every H3 heading (`###`) must be one of the six standard labels: `Added`, `Changed`, `Deprecated`, `Removed`, `Fixed`, or `Security`. Custom or misspelled headings are rejected.

### `unique-headings`
No two version headings may have identical text. Prevents duplicate version entries that would confuse both readers and automated tooling.

### `unique-release`
No two versions may share the same `ref` (e.g., two `[1.0.0]` entries). A duplicate ref makes the changelog ambiguous and breaks reference links.

### `unknown-link-type`
Reference link definitions whose URL pattern matches a known forge but whose link type (compare, release, commit, …) cannot be recognised are warned about. Helps catch misconfigured or unsupported forge URL formats.

### `versioning-format`
Version refs must conform to the active versioning scheme (SemVer, CalVer, or a custom one). Entries that do not match the scheme are flagged so that malformed version strings cannot be released.

---

[← Back to README](../README.md)
