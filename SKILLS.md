# SKILLS.md — Working with Heylogs changelogs

This file is an agent-generic guide (works with any coding assistant) for using
the [Heylogs](https://github.com/nbbrd/heylogs) CLI to validate and maintain
[Keep a Changelog](https://keepachangelog.com)–formatted `CHANGELOG.md` files.

For project architecture and contribution conventions, see [`AGENTS.md`](AGENTS.md).
Full feature docs live under [`docs/`](docs/).

## What Heylogs does

Heylogs parses a Markdown changelog into an AST and validates/transforms it.
It is available as a CLI, a Maven plugin, Maven Enforcer rules, and a Java
library. This guide focuses on the **CLI**.

```bash
heylogs <command> [<args>]
```

## Running the CLI

Examples below write `heylogs …` for brevity. If you don't have a native binary
on `PATH` (e.g. via Homebrew/Scoop), run the same commands through one of the
options below — substitute the whole invocation for `heylogs`.

### With gg.cmd (recommended — no prerequisites)

[`gg.cmd`](https://github.com/eirikb/gg/) bootstraps both a JDK and JBang on
demand, so contributors and CI need **nothing** pre-installed. Download it once
into the repo:

```bash
curl -L ggcmd.io > gg.cmd && chmod +x gg.cmd
```

Then run heylogs via JBang through it:

```bash
./gg.cmd jbang com.github.nbbrd.heylogs:heylogs-cli:0.18.1:bin check CHANGELOG.md
```

### With JBang directly

If JBang is already available:

```bash
jbang com.github.nbbrd.heylogs:heylogs-cli:0.18.1:bin check CHANGELOG.md
```

Install JBang with the universal (all-platforms) installer if needed:

```bash
# Linux / macOS / Windows (bash)
curl -Ls https://sh.jbang.dev | bash -s - app setup
# Windows PowerShell
iex "& { $(iwr -useb https://ps.jbang.dev) } app setup"
```

JBang can also resolve the latest release from the project's catalog
(`heylogs@nbbrd`, requires `trust: https://github.com/nbbrd/jbang-catalog`).
Replace `0.18.1` with the version you want; see
[releases](https://github.com/nbbrd/heylogs/releases/latest). Other install
methods (Homebrew, Scoop, plain jar) are in [`docs/usage-cli.md`](docs/usage-cli.md).

Global options available on every command:

| Option                 | Purpose                                                           |
|------------------------|-------------------------------------------------------------------|
| `--debug`              | Print the full stack trace on error.                              |
| `--batch`              | Suppress progress feedback on stderr and disable ANSI colors (CI).|
| `-D<property>=<value>` | Set a Java system property (repeatable).                          |

Most commands default to `CHANGELOG.md` and accept a positional file argument
(use `-` for stdin). File-modifying commands print a one-line status to
**stderr** (`+` modified, `!` needs attention, `~` dry-run, `=` no-op) and most
support `--dry-run`.

## Primary task: `check`

Validate a changelog against the rule set:

```bash
heylogs check                      # checks CHANGELOG.md
heylogs check path/to/CHANGELOG.md
heylogs check --recursive          # find changelogs recursively
heylogs check --format json --output result.json
```

Output formats: `stylish` (default), `json`, `github-actions`, … (run
`heylogs list` to see what's installed).

### Adjusting which rules run and their severity

Each rule has a severity: `ERROR`, `WARN`, `INFO`, or `OFF` (disabled). Override
per invocation with repeated `--rule <id>:<severity>` options:

```bash
# Enable dot-space-link-style (off by default) as a warning, and disable no-empty-group
heylogs check --rule dot-space-link-style:WARN --rule no-empty-group:OFF
```

This is the canonical way to tune rules ad hoc. For persistent configuration,
use `heylogs.properties` (below).

### Other `check` configuration options

| Option                    | Purpose                                                |
|---------------------------|--------------------------------------------------------|
| `--rule <id:severity>`    | Override a rule's severity (repeatable).               |
| `--versioning <scheme>`   | Versioning scheme: `semver`, `calver:YYYY.MM.DD`, `regex:…`. |
| `--tagging <strategy>`    | Tag naming, e.g. `prefix:v` (for `v1.0.0`).            |
| `--forge <platform>`      | `github`, `gitlab`, or `forgejo` (link validation).    |
| `--domain <domain:forge>` | Map a custom domain to a forge (repeatable), e.g. `git.corp.com:gitlab`. |
| `--no-config`             | Ignore all `heylogs.properties` files.                 |

## Configuration via `heylogs.properties`

Settings can be made persistent (and shared across CLI, Maven plugin, and
Enforcer) in a `heylogs.properties` file. Files are discovered by walking **up**
the directory tree from the changelog's directory; parent files load first and
**child values override parents** (Lombok-style). For list properties (`rules`,
`domains`) the child list **replaces** the parent list entirely.

```properties
# heylogs.properties — project root
versioning=semver
tagging=prefix:v
forge=github

# Rule severity overrides (comma-separated). Same id:severity grammar as --rule.
rules=dot-space-link-style:WARN,no-empty-group:OFF

# Map custom domains to forges (comma-separated)
domains=git.company.com:gitlab,code.internal.org:github

# Stop walking up to parent directories (e.g. monorepo module boundary)
config.stopBubbling=true
```

| Property     | Description                  | Example                                      |
|--------------|------------------------------|----------------------------------------------|
| `versioning` | Version validation scheme    | `semver`, `calver:YYYY.MM.DD`, `regex:^\d+$` |
| `tagging`    | Tag naming strategy          | `prefix:v`                                   |
| `forge`      | Hosting platform             | `github`, `gitlab`, `forgejo`                |
| `rules`      | Rule severity overrides      | `no-empty-group:WARN,https:OFF`              |
| `domains`    | Custom forge domains         | `git.company.com:gitlab`                     |

Precedence note: `--rule` on the command line and the `rules` list in
`heylogs.properties` both express the same `id:severity` overrides. Use
`--no-config` to bypass the file hierarchy entirely and rely only on CLI flags
(useful in CI for explicit, reproducible config).

A template `heylogs.properties` ships at the root of this repository.

### Pattern: get a repo (or several) to "0 problems" without editing the changelog

Because the file is **auto-discovered** and `rules=name:SEVERITY` accepts `OFF`,
rule-tuning can live entirely in configuration — no `CHANGELOG.md` edits needed.
This keeps a changelog a verbatim port of its upstream source while still passing
`heylogs check`.

1. Run `heylogs check` and note every rule id that fires.
2. Put one `heylogs.properties` at the root, listing those rules at the severity
   you want (`OFF` to silence, `WARN` to downgrade from `ERROR`):

   ```properties
   # Uniform config: silence/downgrade the rules that fire, edit no changelogs
   versioning=semver
   forge=github
   rules=dot-space-link-style:OFF,no-empty-group:OFF,release-date:WARN
   ```

3. Re-run `heylogs check` until it reports 0.

For several repositories, drop the **same** `heylogs.properties` in each (or place
one in a shared parent directory and let hierarchical discovery apply it to all,
unless `config.stopBubbling=true` blocks it). One uniform config can bring every
repo to 0 while the workflow and changelog contents stay identical to upstream.

## Available rules

Rules fall into three kinds: **format** (structure/formatting), **content**
(required sections, links, uniqueness), and **versioning** (version numbers and
tag refs). The table below lists every built-in rule and its **default**
severity. The reference description for each is in
[`docs/feature-rules.md`](docs/feature-rules.md).

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
| `no-empty-group`           | ERROR   | A `###` group must have at least one bullet item.                                |
| `no-empty-release`         | ERROR   | A released version must contain at least one item.                              |
| `no-link-brackets`         | ERROR   | Forge reference links not wrapped in `()` or `[]`.                               |
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

## Other commands

All commands default to `CHANGELOG.md`, accept a positional file (or `-` for
stdin), and most support `--dry-run`.

### Authoring / editing

| Command   | Purpose                                                              | Example                                                        |
|-----------|---------------------------------------------------------------------|---------------------------------------------------------------|
| `init`    | Create a new changelog from a template (fails if it exists).        | `heylogs init --project-url https://github.com/nbbrd/heylogs` |
| `push`    | Add an entry to the Unreleased section (creates the group as needed).| `heylogs push -y added -m "Add custom themes"`                |
| `fetch`   | Add an entry from a forge issue/PR (full URL or short `#ref`).      | `heylogs fetch -y fixed -i "#42"`                             |
| `note`    | Set/replace the summary text after the Unreleased header.          | `heylogs note -m "Performance improvements."`                 |
| `format`  | Normalize ordering & markers (idempotent). `--check` for CI gate.   | `heylogs format` · `heylogs format --check`                  |

`push`/`fetch` change types (`-y`): `added`, `changed`, `deprecated`,
`removed`, `fixed`, `security`.

### Releasing

| Command   | Purpose                                              | Example                          |
|-----------|------------------------------------------------------|----------------------------------|
| `release` | Convert Unreleased changes into a new version entry. | `heylogs release --ref 1.0.0`    |
| `yank`    | Append `[YANKED]` to a release heading.              | `heylogs yank -r 1.0.0`          |

### Inspecting / converting

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

## Typical agent workflow

```bash
# 1. Validate; tune rules for this project
heylogs check --rule dot-space-link-style:WARN --rule no-empty-group:OFF

# 2. Normalize structure before committing
heylogs format

# 3. Add changes as you work
heylogs push -y fixed -m "Fix parser memory leak"

# 4. Gate in CI (no colors/feedback, fail on formatting drift)
heylogs --batch format --check
heylogs --batch check
```

In CI, prefer `--batch` (clean output, no ANSI) and consider `--no-config` for
fully explicit, reproducible configuration.

---

See [`docs/`](docs/) for per-feature documentation and
[`docs/feature-matrix.md`](docs/feature-matrix.md) for support by usage mode.
