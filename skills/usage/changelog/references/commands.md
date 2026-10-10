# Heylogs commands reference

All commands default to `CHANGELOG.md`, accept a positional file (or `-` for
stdin), and most support `--dry-run`.

## `check` options

`check` is the primary task (see [`../SKILL.md`](../SKILL.md)). Beyond
`--rule <id>:<severity>`:

| Option                    | Purpose                                                |
|---------------------------|--------------------------------------------------------|
| `--rule <id:severity>`    | Override a rule's severity (repeatable).               |
| `--versioning <scheme>`   | Versioning scheme: `semver`, `calver:YYYY.MM.DD`, `regex:…`. |
| `--tagging <strategy>`    | Tag naming, e.g. `prefix:v` (for `v1.0.0`).            |
| `--forge <platform>`      | `github`, `gitlab`, or `forgejo` (link validation).    |
| `--domain <domain:forge>` | Map a custom domain to a forge (repeatable), e.g. `git.corp.com:gitlab`. |
| `--no-config`             | Ignore all `heylogs.properties` files.                 |

## Authoring / editing

| Command   | Purpose                                                              | Example                                                        |
|-----------|---------------------------------------------------------------------|---------------------------------------------------------------|
| `init`    | Create a new changelog from a template (fails if it exists).        | `heylogs init --project-url https://github.com/nbbrd/heylogs` |
| `push`    | Add an entry to the Unreleased section (creates the group as needed).| `heylogs push -y added -m "Add custom themes"`                |
| `fetch`   | Add an entry from a forge issue/PR (full URL or short `#ref`).      | `heylogs fetch -y fixed -i "#42"`                             |
| `note`    | Set/replace the summary text after the Unreleased header.          | `heylogs note -m "Performance improvements."`                 |
| `format`  | Normalize ordering & markers (idempotent). `--check` for CI gate.   | `heylogs format` · `heylogs format --check`                  |

`push`/`fetch` change types (`-y`): `added`, `changed`, `deprecated`,
`removed`, `fixed`, `security`.

## Releasing

| Command   | Purpose                                              | Example                          |
|-----------|------------------------------------------------------|----------------------------------|
| `release` | Convert Unreleased changes into a new version entry. | `heylogs release --ref 1.0.0`    |
| `yank`    | Append `[YANKED]` to a release heading.              | `heylogs yank -r 1.0.0`          |

## Inspecting / converting

| Command   | Purpose                                                          | Example                                          |
|-----------|------------------------------------------------------------------|--------------------------------------------------|
| `scan`    | Summarize content (release count, date range, forge, …).        | `heylogs scan --format json`                     |
| `extract` | Filter/extract specific versions (e.g. latest only).            | `heylogs extract --limit 1 --output latest.md`   |
| `export`  | Serialize the changelog to structured data (e.g. JSON).         | `heylogs export --format json`                   |
| `import`  | Rebuild a Markdown changelog from structured data (export's inverse). | `heylogs import changelog.json`            |
| `list`    | List installed resources: rules, formats, forges, schemes.      | `heylogs list --format json`                     |

Commands are composable (Unix-style) — `extract` and `export` read from stdin:

```bash
heylogs extract --limit 1 | heylogs export - --format json
```
