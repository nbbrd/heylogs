# Library

Heylogs is available as a **Java library**.  
Its API is straightforward and has a single point of entry:

```java
Heylogs heylogs = Heylogs.ofServiceLoader();
Document flexmarkDocument = parseFileWithFlexmark(file);
Config config = Config.builder().versioningOf("semver").build();
List<Problem> problems = heylogs.check(flexmarkDocument, config);
...
```

> [!WARNING]
> This API is currently in beta and might change frequently.

## Installation

```xml
<dependencies>
   <dependency>
      <groupId>com.github.nbbrd.heylogs</groupId>
      <artifactId>heylogs-api</artifactId>
      <version>_VERSION_</version>
   </dependency>
   <dependency>
      <groupId>com.github.nbbrd.heylogs</groupId>
      <artifactId>heylogs-ext-github</artifactId>
      <version>_VERSION_</version>
      <scope>runtime</scope>
   </dependency>
   ...
</dependencies>
```

---

[← Back to Usage](usage.md)
