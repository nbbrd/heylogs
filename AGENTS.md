# AGENTS.md — Heylogs

## Overview

Heylogs is a Java 8+ multi-module Maven project for validating and releasing changelogs in [Keep a Changelog](https://keepachangelog.com) format. It parses Markdown into an AST (via [flexmark-java](https://github.com/vsch/flexmark-java)) and operates on the tree.

## Architecture

**Core module** (`heylogs-api`): Contains the facade class (`Heylogs`), domain model, and SPI interfaces in the `spi` subpackage. Internal implementation lives under `internal.*` packages — never reference these types from public API.

**Extension modules** (`heylogs-ext-*`): Each implements one or more SPI via `@ServiceProvider` + `@DirectImpl`, delegating to a `*Support` builder when possible. Examples:
- `heylogs-ext-semver` → `SemVer implements Versioning` (delegates to `VersioningSupport`)
- `heylogs-ext-calver` → `CalVer implements Versioning` (delegates to `VersioningSupport`)
- `heylogs-ext-github` → `GitHub implements Forge` (delegates to `ForgeSupport`)
- `heylogs-ext-gitlab` → `GitLab implements Forge` (delegates to `ForgeSupport`)
- `heylogs-ext-forgejo` → `Forgejo implements Forge` (delegates to `ForgeSupport`)
- `heylogs-ext-json` → `JsonFormat implements Format` (delegates to `FormatSupport`)
- `heylogs-ext-http` → `HttpFactory implements HttpFactory` (delegates to `HttpFactorySupport`)

**Consumer modules**: `heylogs-cli` (picocli), `heylogs-maven-plugin` (Maven Mojo), `heylogs-enforcer-rules`. These call the facade's `ofServiceLoader()` factory.

**BOM module** (`heylogs-bom`): Bill of Materials for dependency version management by consumers.

## SPI Extension Pattern

To add a new extension:

1. Create module `<project>-ext-<name>` with a single public class
2. Implement the SPI interface by delegating to the corresponding `*Support` builder
3. Annotate with `@DirectImpl` and `@ServiceProvider`
4. Add the module as `<scope>runtime</scope>` dependency in consumer modules' `pom.xml`
5. IDs must follow `ServiceId.KEBAB_CASE` (enforced by `@ServiceId(pattern = ServiceId.KEBAB_CASE)` on the SPI interface)

```java
// Minimal extension (see any heylogs-ext-* module for real examples)
@DirectImpl
@ServiceProvider
public final class MyImpl implements MySpi {
    @lombok.experimental.Delegate
    private final MySpi delegate = MySpiSupport.builder()
            .id("my-impl")
            .name("My Implementation")
            .moduleId("my-impl")
            // ... SPI-specific builder methods ...
            .build();
}
```

## Build & Test

```shell
mvn clean install                 # full build + tests + enforcer checks
mvn clean install -Pyolo          # skip all checks (fast local iteration)
mvn test -pl <module-name> -Pyolo # fast test a single module
mvn test -pl <module-name> -am    # full test a single module
```

- **Java 8 target** with JPMS `module-info.java` compiled separately on JDK 9+ (see `java8-with-jpms` profile in root POM)
- **JUnit 5** with parallel execution enabled (`junit.jupiter.execution.parallel.enabled=true`); **AssertJ** for assertions
- `heylogs-api` publishes a **test-jar** (`tests/` package) reused by extension modules for shared test fixtures

## Key Conventions

- **Lombok**: use lombok annotations when possible. Config in `lombok.config`: `addNullAnnotations=jspecify`, `builder.className=Builder`
- **Nullability**: `@org.jspecify.annotations.Nullable` for nullable; `@lombok.NonNull` for non-null parameters. Return types use `@Nullable` or the `OrNull` suffix (e.g., `getThingOrNull`)
- **Design annotations** use annotations from `java-design-util` such as `@VisibleForTesting`, `@StaticFactoryMethod`, `@DirectImpl`, `@MightBeGenerated`, `@MightBePromoted`
- **Internal packages**: `internal.<project>.*` are implementation details; public API lives in the root and `spi` packages
- **Static analysis**: `forbiddenapis` (no `jdk-unsafe`, `jdk-deprecated`, `jdk-internal`, `jdk-non-portable`, `jdk-reflection`), `modernizer`
- **Reproducible builds**: `project.build.outputTimestamp` is set in the root POM
- **Formatting/style**: 
  - Use IntelliJ IDEA default code style for Java
  - Follow existing formatting and match naming conventions exactly
  - Follow the principles of "Effective Java"
  - Follow the principles of "Clean Code"
- **Java/JVM**: 
  - Target version defined in root POM properties; some modules may require higher versions
  - Use modern Java feature compatible with defined version

## Agent behavior

- Do respect existing architecture, coding style, and conventions
- Do prefer minimal, reviewable changes
- Do preserve backward compatibility
- Do not introduce new dependencies without justification
- Do not rewrite large sections for cleanliness
- Do not reformat code
- Do not propose additional features or changes beyond the scope of the task
