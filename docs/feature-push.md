# Push command

The push command adds a new change entry to the Unreleased section of a changelog file. It automatically creates the necessary type-of-change section (Added, Changed, Fixed, etc.) if it doesn't exist.

## Usage

### CLI

**Basic Syntax:**
```bash
heylogs push [changelog-file] -t <type> -m "<message>"
```

**Examples:**

```bash
# Add a new feature to Unreleased
heylogs push -t added -m "Add support for custom themes"

# Fix a bug
heylogs push CHANGELOG.md -t fixed -m "Fix memory leak in parser"

# Add a security fix with issue reference
heylogs push -t security -m "Fix XSS vulnerability [#456](https://github.com/user/repo/issues/456)"

# Deprecate a feature
heylogs push -t deprecated -m "Deprecate legacy API endpoints"
```

### Maven Plugin

**Configuration:**
```xml
<plugin>
    <groupId>com.github.nbbrd.heylogs</groupId>
    <artifactId>heylogs-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>push-change</id>
            <goals>
                <goal>push</goal>
            </goals>
            <configuration>
                <type>added</type>
                <message>New feature implementation</message>
            </configuration>
        </execution>
    </executions>
</plugin>
```

**Command-Line:**
```bash
mvn heylogs:push -Dheylogs.type=fixed -Dheylogs.message="Fix critical bug"
```

### Java API

```java
import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.TypeOfChange;
import com.vladsch.flexmark.util.ast.Document;

// Load the changelog document
Document document = parseMarkdown(changelogFile);

// Push a change
Heylogs heylogs = Heylogs.ofServiceLoader();
heylogs.push(document, TypeOfChange.ADDED, "Add new export feature");

// Save the modified document
writeMarkdown(document, changelogFile);
```

---

[← Back to Features](features.md)

