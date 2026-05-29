---
name: create-versioning
description: >
  Use this skill when asked to add, create, or implement a new versioning scheme
  in the Heylogs project. It covers creating the extension module, the public
  SPI class, the internal parser/comparator, the tests, registering the module
  in all POMs, and updating the documentation.
---

Add a new versioning scheme to Heylogs by creating a `heylogs-ext-<id>` module. Execute all steps without asking for confirmation.

## Prerequisites — Gather context

Before starting, read these reference files to understand the patterns:

1. **SPI interface**: `heylogs-api/src/main/java/nbbrd/heylogs/spi/Versioning.java`
2. **Support builder**: `heylogs-api/src/main/java/nbbrd/heylogs/spi/VersioningSupport.java`
3. **Test fixture**: `heylogs-api/src/test/java/tests/heylogs/spi/VersioningAssert.java`
4. **Reference implementation (no-arg)**: `heylogs-ext-semver/src/main/java/nbbrd/heylogs/ext/semver/SemVer.java`
5. **Reference implementation (with-arg)**: `heylogs-ext-calver/src/main/java/nbbrd/heylogs/ext/calver/CalVer.java`
6. **Reference test (no-arg)**: `heylogs-ext-semver/src/test/java/nbbrd/heylogs/ext/semver/SemVerTest.java`
7. **Reference test (with-arg)**: `heylogs-ext-calver/src/test/java/nbbrd/heylogs/ext/calver/CalVerTest.java`
8. **Reference POM (no external dep)**: `heylogs-ext-calver/pom.xml`
9. **Reference POM (external dep)**: `heylogs-ext-semver/pom.xml`

## Step 0 — Determine the versioning characteristics

Decide the following before writing code:

| Decision             | Option A (simpler)            | Option B (complex)                          |
|----------------------|-------------------------------|---------------------------------------------|
| **Argument**         | No argument (`withoutArg`)    | Requires argument (`compilingArg` / custom) |
| **External library** | None (pure regex / algorithm) | External dependency (e.g., `semver4j`)      |
| **Internal classes** | Single internal helper class  | Multiple internal classes                   |

This determines which reference files to follow most closely:
- No arg + no external dep → follow `heylogs-ext-pep440` pattern
- No arg + external dep → follow `heylogs-ext-semver` pattern
- With arg → follow `heylogs-ext-calver` pattern

## Step 1 — Create the module POM

**File**: `heylogs-ext-<id>/pom.xml`

**Action**: Create the file using the template below.

**Template** (no external dependency):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.github.nbbrd.heylogs</groupId>
        <artifactId>heylogs-parent</artifactId>
        <version>0.18.2-SNAPSHOT</version>
    </parent>

    <artifactId>heylogs-ext-<id></artifactId>
    <packaging>jar</packaging>

    <name>heylogs-ext-<id></name>
    <description>Keep-a-changelog tool - <Name> extension</description>
    <url>https://github.com/nbbrd/heylogs</url>

    <dependencies>
        <!-- compile only -->
        <dependency>
            <groupId>org.jspecify</groupId>
            <artifactId>jspecify</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>com.github.nbbrd.java-design-util</groupId>
            <artifactId>java-design-processor</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>com.github.nbbrd.java-service-util</groupId>
            <artifactId>java-service-processor</artifactId>
            <scope>provided</scope>
        </dependency>

        <!-- compile and runtime -->
        <dependency>
            <artifactId>heylogs-api</artifactId>
            <groupId>${project.groupId}</groupId>
            <version>${project.version}</version>
        </dependency>
        <!-- ADD EXTERNAL LIBRARY HERE IF NEEDED -->

        <!-- test only -->
        <dependency>
            <artifactId>heylogs-api</artifactId>
            <groupId>${project.groupId}</groupId>
            <version>${project.version}</version>
            <classifier>tests</classifier>
            <type>test-jar</type>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

> **Note**: Read the current `<version>` from the root `pom.xml` parent tag — do not hardcode the version shown above.

> **External dependency**: If an external library is needed, add it in the `<!-- compile and runtime -->` section and also declare its version in the root POM `<dependencyManagement>` section.

## Step 2 — Create the public SPI class

**File**: `heylogs-ext-<id>/src/main/java/nbbrd/heylogs/ext/<id>/<ClassName>.java`

**Action**: Create the file. The class must:
1. Be `public final` and implement `Versioning`
2. Be annotated with `@DirectImpl` and `@ServiceProvider`
3. Use `@lombok.experimental.Delegate` to delegate to a `VersioningSupport` instance
4. The `id` must be in `KEBAB_CASE` and match the module name convention

**Template (no-arg versioning)**:
```java
package nbbrd.heylogs.ext.<id>;

import internal.heylogs.ext.<id>.<InternalClass>;
import nbbrd.design.DirectImpl;
import nbbrd.heylogs.spi.Versioning;
import nbbrd.heylogs.spi.VersioningSupport;
import nbbrd.service.ServiceProvider;

import static nbbrd.heylogs.spi.VersioningSupport.withoutArg;

@DirectImpl
@ServiceProvider
public final class <ClassName> implements Versioning {

    @lombok.experimental.Delegate
    private final Versioning delegate = VersioningSupport
            .builder()
            .id("<kebab-id>")
            .name("<Human-Readable Name>")
            .urlOf("<spec-url>")
            .moduleId("<kebab-id>")
            .validator(arg -> arg == null ? null : "<Name> does not take any arguments")
            .predicate(withoutArg(<InternalClass>::isValid))
            .comparator(withoutArg(<InternalClass>::compare))
            .familyMapper(withoutArg(<InternalClass>::toFamily))
            .build();
}
```

**Template (with-arg versioning)** — see `CalVer.java` for reference:
```java
package nbbrd.heylogs.ext.<id>;

import internal.heylogs.ext.<id>.<InternalFormat>;
import nbbrd.design.DirectImpl;
import nbbrd.heylogs.spi.Validator;
import nbbrd.heylogs.spi.Versioning;
import nbbrd.heylogs.spi.VersioningSupport;
import nbbrd.service.ServiceProvider;

import java.util.Objects;

import static nbbrd.heylogs.spi.VersioningSupport.compilingArg;

@DirectImpl
@ServiceProvider
public final class <ClassName> implements Versioning {

    @lombok.experimental.Delegate
    private final Versioning delegate = VersioningSupport
            .builder()
            .id("<kebab-id>")
            .name("<Human-Readable Name>")
            .urlOf("<spec-url>")
            .moduleId("<kebab-id>")
            .validator(Validator.of(<InternalFormat>::parse))
            .predicate(compilingArg(<InternalFormat>::parse, <InternalFormat>::isValidVersion))
            .comparator(arg -> {
                <InternalFormat> format = <InternalFormat>.parse(Objects.requireNonNull(arg));
                return (a, b) -> compare(format, a, b);
            })
            .familyMapper(arg -> {
                <InternalFormat> format = <InternalFormat>.parse(Objects.requireNonNull(arg));
                return version -> toFamily(format, version);
            })
            .build();

    // Add private static helper methods for compare and toFamily
}
```

### `VersioningSupport` builder fields reference

| Field          | Type                                               | Required | Description                                                         |
|----------------|----------------------------------------------------|----------|---------------------------------------------------------------------|
| `id`           | `String`                                           | Yes      | Kebab-case identifier (e.g., `"pep440"`, `"semver"`)                |
| `name`         | `String`                                           | Yes      | Human-readable name                                                 |
| `url`/`urlOf`  | `URL`/`CharSequence`                               | Yes      | Link to the versioning spec                                         |
| `moduleId`     | `String`                                           | Yes      | Usually same as `id`                                                |
| `validator`    | `Function<String, String>`                         | Yes      | Returns null if arg valid, error message otherwise                  |
| `predicate`    | `Function<String, Predicate<CharSequence>>`        | Yes      | Tests if a string is a valid version                                |
| `comparator`   | `Function<String, Comparator<CharSequence>>`       | No       | Compares two versions; return 0 for incomparable                    |
| `familyMapper` | `Function<String, Function<CharSequence, String>>` | No       | Maps version to family key (e.g., `"MAJOR.MINOR"`); null if invalid |

### Helper methods in `VersioningSupport`

| Method                      | Use when                                   | Wraps argument handling             |
|-----------------------------|--------------------------------------------|-------------------------------------|
| `withoutArg(Predicate)`     | No argument needed                         | Returns predicate when arg is null  |
| `withoutArg(Comparator)`    | No argument needed                         | Returns comparator when arg is null |
| `withoutArg(Function)`      | No argument needed                         | Returns mapper when arg is null     |
| `compilingArg(factory, bi)` | Argument is compiled into an engine object | Compiles arg, then delegates        |

## Step 3 — Create the internal helper class

**File**: `heylogs-ext-<id>/src/main/java/internal/heylogs/ext/<id>/<InternalClass>.java`

**Action**: Create the file with the parsing, validation, comparison, and family-mapping logic.

**Required static methods for no-arg versioning**:

```java
package internal.heylogs.ext.<id>;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

public final class <InternalClass> {

    private <InternalClass>() {
        throw new UnsupportedOperationException();
    }

    /** Returns true if text is a valid <scheme> version string. */
    public static boolean isValid(@NonNull CharSequence text) { ... }

    /**
     * Compares two version strings.
     * Returns 0 if either is invalid (incomparable).
     */
    public static int compare(@NonNull CharSequence a, @NonNull CharSequence b) { ... }

    /**
     * Returns the family key (typically "MAJOR.MINOR"), or null if invalid.
     */
    public static @Nullable String toFamily(@NonNull CharSequence version) { ... }
}
```

**Implementation guidelines**:
- Use `java.util.regex.Pattern` for validation (compile as a `private static final` field)
- Return `0` from `compare` when either input is invalid (incomparable)
- Return `null` from `toFamily` when input is invalid
- Family is typically `"MAJOR.MINOR"` (first two numeric components)
- Target Java 8: no `var`, no `String.repeat()`, no `Stream.toList()`, no `List.of()`
- Use `Locale.ROOT` for any case conversions

## Step 4 — Create the test class

**File**: `heylogs-ext-<id>/src/test/java/nbbrd/heylogs/ext/<id>/<ClassName>Test.java`

**Action**: Create the file with four test methods.

**Template (no-arg)**:
```java
package nbbrd.heylogs.ext.<id>;

import nbbrd.heylogs.spi.Versioning;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.VersioningAssert.assertVersioningCompliance;

class <ClassName>Test {

    @Test
    public void testCompliance() {
        assertVersioningCompliance(new <ClassName>());
    }

    @Test
    void testIsValidVersion() {
        Versioning x = new <ClassName>();

        // Verify that passing an argument is rejected
        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getVersioningPredicateOrNull(""))
                .withMessage("<Name> does not take any arguments");

        // Verify valid and invalid versions
        assertThat(x.getVersioningPredicateOrNull(null))
                .accepts(/* valid version examples */)
                .rejects(/* invalid version examples */);
    }

    @Test
    void testComparator() {
        Versioning x = new <ClassName>();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getVersioningComparatorOrNull(""))
                .withMessage("<Name> does not take any arguments");

        assertThat(x.getVersioningComparatorOrNull(null))
                .isNotNull()
                .satisfies(comparator -> {
                    // basic ordering
                    assertThat(comparator.compare("2.0.0", "1.0.0")).isPositive();
                    assertThat(comparator.compare("1.0.0", "2.0.0")).isNegative();
                    assertThat(comparator.compare("1.0.0", "1.0.0")).isZero();

                    // scheme-specific ordering tests

                    // incomparable
                    assertThat(comparator.compare("invalid", "1.0.0")).isZero();
                });
    }

    @Test
    void testFamilyMapper() {
        Versioning x = new <ClassName>();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getVersioningFamilyMapperOrNull(""))
                .withMessage("<Name> does not take any arguments");

        assertThat(x.getVersioningFamilyMapperOrNull(null))
                .isNotNull()
                .satisfies(mapper -> {
                    assertThat(mapper.apply("2.4.0")).isEqualTo("2.4");
                    assertThat(mapper.apply("2.4.1")).isEqualTo("2.4");
                    assertThat(mapper.apply("invalid")).isNull();
                });
    }
}
```

**Template (with-arg)** — see `CalVerTest.java` for reference. Key differences:
- Passing `null` arg throws `IllegalArgumentException` (instead of being accepted)
- Tests pass a format string argument to each method

**Test guidelines**:
- Always include a `testCompliance()` method calling `assertVersioningCompliance`
- Test at least 3 valid and 3 invalid versions in `testIsValidVersion`
- Test basic ordering, scheme-specific ordering, and incomparable cases in `testComparator`
- Test family mapping for multiple versions and the invalid case in `testFamilyMapper`
- Use AssertJ fluent assertions

## Step 5 — Register the module in POMs

Use `replace_string_in_file` for each of the following files. Insert the new module adjacent to existing versioning extensions (after `heylogs-ext-calver` or `heylogs-ext-semver`).

### 5a — Root POM (`pom.xml`)

Add `<module>heylogs-ext-<id></module>` in the `<modules>` section:
```xml
<module>heylogs-ext-<id></module>
```

### 5b — BOM (`heylogs-bom/pom.xml`)

Add a `<dependency>` entry in `<dependencyManagement><dependencies>`:
```xml
<dependency>
    <artifactId>heylogs-ext-<id></artifactId>
    <groupId>${project.groupId}</groupId>
    <version>${project.version}</version>
</dependency>
```

### 5c — Consumer modules (runtime dependency)

Add a `<scope>runtime</scope>` dependency in each of:
- `heylogs-cli/pom.xml`
- `heylogs-maven-plugin/pom.xml`
- `heylogs-enforcer-rules/pom.xml`

```xml
<dependency>
    <artifactId>heylogs-ext-<id></artifactId>
    <groupId>${project.groupId}</groupId>
    <version>${project.version}</version>
    <scope>runtime</scope>
</dependency>
```

### 5d — Architecture tests (`heylogs-architecture-tests/pom.xml`)

Add a `<scope>test</scope>` dependency:
```xml
<dependency>
    <groupId>com.github.nbbrd.heylogs</groupId>
    <artifactId>heylogs-ext-<id></artifactId>
    <version>${project.version}</version>
    <scope>test</scope>
</dependency>
```

## Step 6 — Update documentation

### Versioning table (`docs/feature-versioning.md`)

Add a row to the versioning schemes table in **alphabetical order** by ID:

```markdown
| `<id>` | [<Name>](<spec-url>) | `<example>` | -              |
```

The argument column should be `-` for no-arg versionings, or a description for with-arg.

Also add a usage example:
```markdown
- `$ heylogs check -v <id>`
```

## Post-implementation checks

**Validation**: After all edits, call `get_errors` on these files:
- `heylogs-ext-<id>/src/main/java/nbbrd/heylogs/ext/<id>/<ClassName>.java`
- `heylogs-ext-<id>/src/main/java/internal/heylogs/ext/<id>/<InternalClass>.java`
- `heylogs-ext-<id>/src/test/java/nbbrd/heylogs/ext/<id>/<ClassName>Test.java`

## Testing

```shell
# Fast test of the new module
mvn test -pl heylogs-ext-<id> -am -Pyolo

# Full build with all checks
mvn clean install
```

Expect:
- 4 tests run (compliance + isValidVersion + comparator + familyMapper)
- 0 failures, 0 errors
- BUILD SUCCESS

## Constraints

- **ID** must match `ServiceId.KEBAB_CASE` (lowercase letters, digits, hyphens; e.g., `pep440`, `semver`, `calver`).
- **Java 8 target**: no `var`, `String.repeat()`, `Stream.toList()`, `List.of()`, `Map.of()`.
- **No BOM in source files**: if creating files via PowerShell, use `[System.IO.File]::WriteAllText(path, content, (New-Object System.Text.UTF8Encoding $false))` to avoid UTF-8 BOM bytes.
- **Internal packages**: parsing/comparison logic goes in `internal.heylogs.ext.<id>` — never expose internal types in the public API.
- **Annotations**: `@DirectImpl` + `@ServiceProvider` on the public class; `@lombok.experimental.Delegate` on the delegate field.
- **Backward compatibility**: the `Versioning` SPI contract requires `getVersioningPredicateOrNull` to throw `IllegalArgumentException` when the validator rejects the arg.
- **Incomparable versions**: `compare()` must return `0` when either input is not a valid version for the scheme.
- **Family mapping**: `toFamily()` must return `null` when the input is not a valid version.
- **No additional features**: do not add features beyond what the `Versioning` SPI defines.

## Quick reference — Existing versioning modules

| Module               | ID       | Arg? | External dep?    | Internal class(es)                |
|----------------------|----------|------|------------------|-----------------------------------|
| `heylogs-ext-semver` | `semver` | No   | `semver4j`       | (none — uses library directly)    |
| `heylogs-ext-calver` | `calver` | Yes  | No               | `CalVerFormat`, `CalVerTag`, etc. |
| `heylogs-ext-pep440` | `pep440` | No   | No               | `Pep440Version`                   |
| `heylogs-ext-maven`  | `maven`  | No   | `maven-artifact` | (none — uses library directly)    |

