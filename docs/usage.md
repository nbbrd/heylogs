# Usage

Heylogs is available in multiple formats to fit your workflow:

- **[Library](usage-library.md)** - Java API for programmatic access
- **[Command-line tool](usage-cli.md)** - Command-line tool for any platform
- **[Maven plugin](usage-maven-plugin.md)** - Maven build integration
- **[Maven Enforcer rules](usage-enforcer-rules.md)** - Enforce changelog quality in builds

Most probably, one wants to check the `CHANGELOG.md` file in a CI/CD environment, thus the command is as follows:

```yml
- uses: jbangdev/jbang-action@v0.130.0
  with:
    script: com.github.nbbrd.heylogs:heylogs-cli:_VERSION_:bin
    scriptargs: "check -o result.json"
```

An alternative is to use the Maven command-line:

```yml
- run: mvn com.github.nbbrd.heylogs:heylogs-maven-plugin:_VERSION_:check -D heylogs.outputFile=result.json
```

---

[← Back to README](../README.md)
