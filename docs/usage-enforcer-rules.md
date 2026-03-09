# Maven Enforcer rules

The heylogs Maven Enforcer rule `checkChangelog` integrates changelog validation directly into Maven's enforcer plugin, allowing you to enforce changelog quality standards as part of your build process. The build fails if the changelog doesn't meet the configured requirements.

## Quick Start

Add the enforcer plugin with the heylogs custom rule to your pom.xml:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-enforcer-plugin</artifactId>
            <version>3.5.0</version>
            <dependencies>
                <dependency>
                    <groupId>com.github.nbbrd.heylogs</groupId>
                    <artifactId>heylogs-enforcer-rules</artifactId>
                    <version>_VERSION_</version>
                </dependency>
            </dependencies>
            <executions>
                <execution>
                    <id>enforce-changelog</id>
                    <goals>
                        <goal>enforce</goal>
                    </goals>
                    <configuration>
                        <rules>
                            <checkChangelog>
                                <versioning>semver</versioning>
                            </checkChangelog>
                        </rules>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Configuration Parameters

### Input/Output Parameters

#### inputFiles
List of changelog files to validate.

**Type:** `List<File>`  
**Default:** `${project.basedir}/CHANGELOG.md`  
**Example:**
```xml
<inputFiles>
    <inputFile>${project.basedir}/CHANGELOG.md</inputFile>
    <inputFile>${project.basedir}/docs/HISTORY.md</inputFile>
</inputFiles>
```

#### recursive
Search for changelog files recursively in directories.

**Type:** `boolean`  
**Default:** `false`  
**Example:**
```xml
<recursive>true</recursive>
```

#### outputFile
File to write validation results to.

**Type:** `File`  
**Default:** stdout (console output)  
**Example:**
```xml
<outputFile>${project.build.directory}/changelog-report.txt</outputFile>
```

### Validation Parameters

#### versioning
Versioning scheme to validate version numbers.

**Type:** `String`  
**Default:** none  
**Values:** `semver`, `calver:PATTERN`, `regex:PATTERN`  
**Example:**
```xml
<versioning>semver</versioning>
```

#### tagging
Tag naming strategy.

**Type:** `String`  
**Default:** none  
**Example:**
```xml
<tagging>prefix:v</tagging>
```

#### forge
Source code hosting platform.

**Type:** `String`  
**Default:** none  
**Values:** `github`, `gitlab`, `forgejo`  
**Example:**
```xml
<forge>github</forge>
```

#### rules
Rule severity overrides (comma-separated).

**Type:** `List<String>`  
**Default:** none  
**Example:**
```xml
<rules>
    <rule>no-empty-group:WARN</rule>
    <rule>release-date:ERROR</rule>
</rules>
```

#### domains
Custom forge domain mappings.

**Type:** `List<String>`  
**Default:** none  
**Example:**
```xml
<domains>
    <domain>gitlab.company.com:gitlab</domain>
    <domain>github.enterprise.com:github</domain>
</domains>
```

#### format
Output format for validation results.

**Type:** `String`  
**Default:** `stylish` (human-readable)  
**Values:** `stylish`, `json`  
**Example:**
```xml
<format>json</format>
```

### Control Parameters

#### skip
Skip the enforcer rule execution.

**Type:** `boolean`  
**Default:** `false`  
**Example:**
```xml
<skip>true</skip>
```

Or via command-line:
```bash
mvn clean install -Denforcer.skip=true
```

#### noConfig
Ignore heylogs.properties configuration files.

**Type:** `boolean`  
**Default:** `false`  
**Example:**
```xml
<noConfig>true</noConfig>
```

---

[← Back to README](../README.md)

