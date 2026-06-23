---
name: heylogs
description: >
  Validate and maintain Keep a Changelog–formatted CHANGELOG.md files with the
  Heylogs CLI. Use this skill whenever the user asks to check, lint, validate,
  fix, format, or release a changelog, sees Heylogs rule violations (e.g.
  no-empty-group, latest-version-first, forge-ref), wants to get a repo to
  "0 problems" via heylogs.properties without editing the changelog, or needs to
  add/extract/export changelog entries. Covers the `check` command and rule
  configuration (primary task), persistent `heylogs.properties` config, every
  built-in rule, the authoring/releasing/inspecting commands, and how to run the
  CLI with no prerequisites via JBang.
metadata:
  source: https://github.com/nbbrd/heylogs
  agent-generic: "true"
---

# Working with Heylogs changelogs

Agent-generic guide (works with any coding assistant) for using the
[Heylogs](https://github.com/nbbrd/heylogs) CLI to validate and maintain
[Keep a Changelog](https://keepachangelog.com)–formatted `CHANGELOG.md` files.

For project architecture and contribution conventions, see [`AGENTS.md`](../../AGENTS.md).
Full feature docs live under [`docs/`](../../docs/).

## What Heylogs does

Heylogs parses a Markdown changelog into an AST and validates/transforms it.
It is available as a CLI, a Maven plugin, Maven Enforcer rules, and a Java
library. This skill focuses on the **CLI**.

```bash
heylogs <command> [<args>]
```

Examples below write `heylogs …` for brevity. If you don't have a native binary
on `PATH`, run the same command through JBang (no prerequisites) — see
[`references/running-cli.md`](references/running-cli.md). Quickest one-liner:

```bash
export JBANG_USE_NATIVE=true
curl -Ls https://sh.jbang.dev | bash -s - com.github.nbbrd.heylogs:heylogs-cli:0.18.1:bin check CHANGELOG.md
```

Most commands default to `CHANGELOG.md` and accept a positional file argument
(use `-` for stdin). File-modifying commands print a one-line status to
**stderr** (`+` modified, `!` needs attention, `~` dry-run, `=` no-op) and most
support `--dry-run`. Global options (`--debug`, `--batch`, `-D<prop>=<val>`) are
documented in [`references/running-cli.md`](references/running-cli.md).

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
use `heylogs.properties` (below). Other `check` options — `--versioning`,
`--tagging`, `--forge`, `--domain`, `--no-config` — are listed in
[`references/commands.md`](references/commands.md#check-options).

## Configuration via `heylogs.properties`

Settings can be made persistent (and shared across CLI, Maven plugin, and
Enforcer) in a `heylogs.properties` file. Files are discovered by walking **up**
the directory tree from the changelog's directory; parent files load first and
**child values override parents**. A template ships at the root of the Heylogs
repository. Full discovery/override/`stopBubbling` semantics and the property
table are in [`references/config-file.md`](references/config-file.md).

```properties
# heylogs.properties — project root
versioning=semver
tagging=prefix:v
forge=github
rules=dot-space-link-style:WARN,no-empty-group:OFF
```

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
unless `config.stopBubbling=true` blocks it).

## Available rules

Rules fall into three kinds: **format** (structure/formatting), **content**
(required sections, links, uniqueness), and **versioning** (version numbers and
tag refs). The full table of every built-in rule with its default severity is in
[`references/rules.md`](references/rules.md). Discover the rules installed in
your environment (including extensions) with:

```bash
heylogs list                 # human-readable; also lists formats, forges, schemes
heylogs list --format json
```

## Other commands

All commands default to `CHANGELOG.md`, accept a positional file (or `-` for
stdin), and most support `--dry-run`:

- **Authoring/editing:** `init`, `push`, `fetch`, `note`, `format`
- **Releasing:** `release`, `yank`
- **Inspecting/converting:** `scan`, `extract`, `export`, `import`, `list`

Full purpose + examples for each, plus composability notes, are in
[`references/commands.md`](references/commands.md).

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

See [`docs/`](../../docs/) for per-feature documentation and
[`docs/feature-matrix.md`](../../docs/feature-matrix.md) for support by usage mode.
