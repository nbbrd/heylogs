# Install the `changelog` skill

The `changelog` skill teaches an AI coding agent how to validate and maintain
[Keep a Changelog](https://keepachangelog.com)–formatted `CHANGELOG.md` files
with the **[Heylogs](https://github.com/nbbrd/heylogs)** CLI — running `check`,
tuning rules, and configuring `heylogs.properties`. The skill definition is in
[`SKILL.md`](SKILL.md), with details under [`references/`](references/).

Skills can be installed in many ways — refer to your agent's documentation for
the available options. Below we use [skills](https://github.com/vercel-labs/skills)
(`npx skills`, see [skills.sh](https://skills.sh)), which installs a skill in a
single step across multiple agents (Claude Code, OpenCode, GitHub Copilot,
Codex, and more) in a unified way.

## Prerequisites

Node.js (v18+) is required only for the `npx skills` method. Install it via your
system's package manager or from [nodejs.org](https://nodejs.org).

The skill drives the Heylogs CLI; if it isn't on your `PATH`, the skill itself
explains how to run it with no prerequisites via JBang (see
[`references/running-cli.md`](references/running-cli.md)).

## Installation

```bash
npx skills add nbbrd/heylogs
```

The installer fetches the skill from the repository, asks which agents to install
it for (several universal agents incl. Claude Code are enabled by default), then
the scope (project vs **Global** — recommended so it's available in every
project) and method (**Symlink** recommended — updates reflect immediately).
Confirm to finish.

Non-interactive (CI-friendly) install for Claude Code, global:

```bash
npx skills add nbbrd/heylogs -g -a claude-code -y
```

## Alternative: manual installation

If you prefer not to use `npx skills`, download the skill folder
[`skills/usage/changelog/`](.) and register it as a local skill folder per your agent's
documentation (no Node.js required).

## Update

```bash
npx skills update changelog
```

## Usage

Once installed, invoke `/changelog` in the selected agent, or just ask it to check,
fix, or release a changelog — the skill's `description` triggers it automatically.
See the [skill definition](SKILL.md) for full capabilities.
