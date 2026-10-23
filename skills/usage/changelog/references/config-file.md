# Configuration via `heylogs.properties`

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

A template `heylogs.properties` ships at the root of the Heylogs repository.

## Pattern: get a repo (or several) to "0 problems" without editing the changelog

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
