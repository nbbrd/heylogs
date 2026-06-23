# Running the Heylogs CLI

Examples in [`../SKILL.md`](../SKILL.md) write `heylogs …` for brevity. If you
don't have a native binary on `PATH` (e.g. via Homebrew/Scoop), run the same
commands through one of the options below — substitute the whole invocation for
`heylogs`.

## Zero-install one-liner (recommended — no prerequisites)

JBang's install script bootstraps JBang (and a JDK) on demand and runs the CLI in
a single command — nothing pre-installed. Set `JBANG_USE_NATIVE=true` to launch
JBang's own native binary for near-instant startup (no JVM needed to start JBang
itself):

```bash
# Linux / macOS / Windows (bash)
export JBANG_USE_NATIVE=true
curl -Ls https://sh.jbang.dev | bash -s - com.github.nbbrd.heylogs:heylogs-cli:0.18.1:bin check CHANGELOG.md
```

```powershell
# Windows PowerShell
$env:JBANG_USE_NATIVE = "true"
iex "& { $(iwr -useb https://ps.jbang.dev) } com.github.nbbrd.heylogs:heylogs-cli:0.18.1:bin check CHANGELOG.md"
```

## With JBang installed

If JBang is already on `PATH`:

```bash
jbang com.github.nbbrd.heylogs:heylogs-cli:0.18.1:bin check CHANGELOG.md
```

Install JBang persistently with the universal (all-platforms) installer if needed:

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
methods (Homebrew, Scoop, plain jar) are in
[`docs/usage-cli.md`](../../../docs/usage-cli.md).

## Global options

Available on every command:

| Option                 | Purpose                                                           |
|------------------------|-------------------------------------------------------------------|
| `--debug`              | Print the full stack trace on error.                              |
| `--batch`              | Suppress progress feedback on stderr and disable ANSI colors (CI).|
| `-D<property>=<value>` | Set a Java system property (repeatable).                          |

Most commands default to `CHANGELOG.md` and accept a positional file argument
(use `-` for stdin). File-modifying commands print a one-line status to
**stderr** (`+` modified, `!` needs attention, `~` dry-run, `=` no-op) and most
support `--dry-run`.
