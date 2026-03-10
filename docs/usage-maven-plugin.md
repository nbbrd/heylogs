# Maven plugin

**Heylogs Maven plugin** allows the tool to be part of a Maven build workflow but can also be used as a standalone tool.

## Examples

Check the changelog automatically on every build:

```xml
<plugin>
    <groupId>com.github.nbbrd.heylogs</groupId>
    <artifactId>heylogs-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
            <inherited>false</inherited>
        </execution>
    </executions>
    <configuration>
        <versioning>semver</versioning>
    </configuration>
</plugin>
```

> [!TIP]
> Versioning, tagging and forge should be configured globally in the plugin configuration section
> instead of the execution configuration section to avoid repetition and basic mistakes.

This check can also be performed manually by calling it directly from the command-line:

```shell
mvn heylogs:check
```

---

[← Back to Usage](usage.md)
