# Running the Heylogs CLI

Examples in [`../SKILL.md`](../SKILL.md) write `heylogs …` for brevity. If you
don't have a native binary on `PATH` (e.g. via Homebrew/Scoop), run the same
commands through one of the options below — substitute the whole invocation for
`heylogs`.

## With gg.cmd (recommended — no prerequisites)

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

## With JBang directly

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
