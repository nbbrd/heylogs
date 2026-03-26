# Tips and best practices

This page provides practical tips and best practices for using Heylogs effectively in your projects.

## Table of contents

- [General tips](#general-tips)
- [Use stdin and stdout with `-`](#use-stdin-and-stdout-with--)
- [Use Maven plugin without a `pom.xml` as a fallback](#use-maven-plugin-without-a-pomxml-as-a-fallback)
- [Recover full changelog from release failure](#recover-full-changelog-from-release-failure)

## General tips

- **Automate changelog checks:** Integrate Heylogs into your CI/CD pipeline to catch issues early.
- **Use configuration files:** Place a `heylogs.properties` file at the root of your repository for consistent rule enforcement.
- **Keep changelogs up to date:** Use the `push` command to add changes as you work, not just before releases.
- **Customize rules:** Adjust rule severities and enable/disable rules to fit your workflow.
- **Validate before release:** Always run `heylogs check` before publishing a new release.
- **Leverage CLI composition:** Combine Heylogs with other tools (e.g., `curl`, `jq`, `bat`) for powerful workflows.

## Use stdin and stdout with `-`

Commands that read a changelog file accept `-` as the file argument to read from **stdin**.  
Commands that write a result accept `-` as the `-o` argument to write to **stdout**.

```shell
# Check a changelog piped from another command
cat CHANGELOG.md | heylogs check -

# Extract a version from stdin and print to stdout
cat CHANGELOG.md | heylogs extract - --ref 1.2.3

# Scan from stdin, write summary to a file
cat CHANGELOG.md | heylogs scan - -o summary.txt

# Explicit stdout output (equivalent to the default for read-only commands)
heylogs scan CHANGELOG.md -o -
```

Commands that modify the changelog in place (`release`, `push`) do not support `-` because they read and write the same file.

## Use Maven plugin without a `pom.xml` as a fallback

If the Heylogs CLI is not available but Maven is installed, you can invoke the Maven plugin directly using its full coordinates — no `pom.xml` required.
Maven resolves and downloads the plugin on the fly from the central repository.

```shell
# Check a changelog
mvn com.github.nbbrd.heylogs:heylogs-maven-plugin:0.14.0:check -Dheylogs.inputFile=CHANGELOG.md

# Scan a changelog
mvn com.github.nbbrd.heylogs:heylogs-maven-plugin:0.14.0:scan -Dheylogs.inputFile=CHANGELOG.md

# Release the Unreleased section
mvn com.github.nbbrd.heylogs:heylogs-maven-plugin:0.14.0:release -Dheylogs.inputFile=CHANGELOG.md -Dheylogs.ref=1.2.3
```

Replace `0.14.0` with the latest stable version.
This approach is especially useful in CI/CD environments or on machines where only Maven is available.

## Recover full changelog from release failure

If a release (for example `v1.2.0`) is impossible for whatever reason,
and you have to patch it and increment the version (for example `v1.2.1`) to solve the problem,
you can use this command to get the full release note into your clipboard and paste it where necessary:

```shell
heylogs extract --ref 1.2 | clip
```

This command will extract both 1.2.0 and 1.2.1 release notes and copy them to your clipboard.

---

[← Back to README](../README.md)
