# Using Heylogs in CI/CD pipelines

Heylogs can be integrated into CI/CD environments to automate changelog validation, release, and quality checks. This ensures that your changelog remains consistent and up-to-date throughout your development lifecycle.

## GitHub Actions

**Check changelog format with inline annotations:**
```yml
- uses: jbangdev/jbang-action@v0.130.0
  with:
    script: com.github.nbbrd.heylogs:heylogs-cli:_VERSION_:bin
    scriptargs: "check --format github-actions"
```

This format emits GitHub Actions workflow commands that appear as inline annotations on the Files changed tab of Pull Requests. Errors are highlighted directly in the changelog file, making it easier for developers to identify and fix issues without digging through logs.

**Check changelog format (JSON output):**
```yml
- uses: jbangdev/jbang-action@v0.130.0
  with:
    script: com.github.nbbrd.heylogs:heylogs-cli:_VERSION_:bin
    scriptargs: "check -o result.json"
```

**Release changelog:**
```yml
- uses: jbangdev/jbang-action@v0.130.0
  with:
    script: com.github.nbbrd.heylogs:heylogs-cli:_VERSION_:bin
    scriptargs: "release --ref ${{ github.ref_name }}"
```

## Maven CI/CD

An alternative to GitHub Actions is to use the Maven command-line in your CI/CD pipeline using this syntax:
```shell
mvn com.github.nbbrd.heylogs:heylogs-maven-plugin:_VERSION_:<command> -D heylogs.<option>=<value>
```

**Check changelog format:**
```shell
mvn com.github.nbbrd.heylogs:heylogs-maven-plugin:_VERSION_:check -D heylogs.outputFile=result.json
```

**Release changelog:**
```shell
mvn com.github.nbbrd.heylogs:heylogs-maven-plugin:_VERSION_:release -D heylogs.ref=1.2.3
```

## General tips

- Use the `check` command to validate changelog quality before merging or releasing.
- Use the `release` command to finalize unreleased changes as part of your release process.
- Use the `push` command to automate changelog updates from pull requests or commit messages.
- Configure rules and options in `heylogs.properties` for consistent results across environments.

---

[← Back to README](../README.md)
