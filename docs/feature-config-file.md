# Configuration file

Heylogs supports hierarchical configuration through `heylogs.properties` files, similar to [Lombok's configuration system](https://projectlombok.org/features/configuration). Configuration files are discovered by walking up the directory tree, with child configurations overriding parent values.

## Quick Start

Create a `heylogs.properties` file in your project root:

```properties
# Basic configuration
versioning=semver
tagging=prefix:v
forge=github
```

Heylogs automatically discovers and applies this configuration when run from any subdirectory.

## File Location and Discovery

### Search Process

Heylogs searches for `heylogs.properties` files by:
1. Starting from the **changelog file's directory** (or current directory)
2. Walking **up the directory tree** to the filesystem root
3. Loading all discovered files in **parent-to-child order**
4. Merging configurations with **child values taking precedence**

### Example Directory Structure

```
/project/                          # heylogs.properties (versioning=semver)
├── heylogs.properties
├── module-a/                      # heylogs.properties (forge=github)
│   ├── heylogs.properties
│   └── CHANGELOG.md              # Uses: semver + github
└── module-b/                      # heylogs.properties (forge=gitlab)
    ├── heylogs.properties
    └── CHANGELOG.md              # Uses: semver + gitlab
```

When checking `module-a/CHANGELOG.md`:
- Loads `/project/heylogs.properties` (versioning=semver)
- Loads `/project/module-a/heylogs.properties` (forge=github)
- Merges: `versioning=semver, forge=github`

## Configuration Properties

### Available Properties

| Property     | Description                  | Example                                      |
|--------------|------------------------------|----------------------------------------------|
| `versioning` | Version validation scheme    | `semver`, `calver:YYYY.MM.DD`, `regex:^\d+$` |
| `tagging`    | Tag naming strategy          | `prefix:v`                                   |
| `forge`      | Source code hosting platform | `github`, `gitlab`, `forgejo`                |
| `rules`      | Rule severity overrides      | `no-empty-group:WARN,https:OFF`              |
| `domains`    | Custom forge domains         | `git.company.com:gitlab,internal.org:github` |

### Property Details

#### versioning
Specifies how version numbers are validated.

```properties
# Semantic Versioning
versioning=semver

# Calendar Versioning
versioning=calver:YYYY.MM.DD

# Custom regex
versioning=regex:^v?\d+\.\d+$
```

#### tagging
Defines tag prefix for version references.

```properties
# Tags like v1.0.0, v2.1.3
tagging=prefix:v

# Tags without prefix: 1.0.0, 2.1.3
tagging=prefix:
```

#### forge
Specifies the default forge for link validation.

```properties
forge=github   # GitHub
forge=gitlab   # GitLab
forge=forgejo  # Forgejo
```

#### rules
Comma-separated list of rule severity overrides.

```properties
# Multiple rules
rules=no-empty-group:WARN,https:OFF,release-date:ERROR

# Disable a rule
rules=dot-space-link-style:OFF
```

#### domains
Comma-separated list of domain-to-forge mappings.

```properties
# Map custom domains to forges
domains=git.company.com:gitlab,code.internal.org:github

# Multiple custom domains
domains=gitlab.mycompany.com:gitlab,github.enterprise.com:github
```

## Hierarchical Configuration

### Merging Rules

When multiple `heylogs.properties` files exist in the directory hierarchy:

**Simple Properties** (versioning, tagging, forge):
- Child value **replaces** parent value
- If child doesn't specify, parent value is inherited

**List Properties** (rules, domains):
- Child list **completely replaces** parent list
- Empty child list removes parent list

### Example: Multi-Level Override

```
/company/
├── heylogs.properties          # Root config
│   versioning=semver
│   forge=github
│   rules=https:ERROR
│
├── backend/
│   ├── heylogs.properties      # Backend config
│   │   forge=gitlab
│   │   rules=https:WARN,no-empty-release:ERROR
│   │
│   └── api/
│       └── CHANGELOG.md
│
└── frontend/
    ├── heylogs.properties      # Frontend config
    │   tagging=prefix:v
    │
    └── CHANGELOG.md
```

**For `/company/backend/api/CHANGELOG.md`:**
```properties
versioning=semver              # From root
forge=gitlab                   # Overridden by backend
rules=https:WARN,no-empty-release:ERROR  # Replaced by backend (https changed from ERROR to WARN)
tagging=(not set)              # Not inherited from frontend
```

**For `/company/frontend/CHANGELOG.md`:**
```properties
versioning=semver              # From root
forge=github                   # From root
tagging=prefix:v              # From frontend
rules=https:ERROR             # From root
```

## Stop Bubbling

Prevent searching parent directories using `config.stopBubbling`:

```properties
# Stop looking for parent configs
config.stopBubbling=true

versioning=semver
forge=github
```

**Use Cases:**
- Monorepo with independent modules
- Isolated subprojects
- Test directories with different rules

## Ignoring config files

The `--no-config` option disables all `heylogs.properties` file discovery, using only command-line options or defaults.

**CLI Usage:**
```bash
heylogs check --no-config --versioning semver --forge github
```

**Maven Plugin:**
```xml
<configuration>
    <noConfig>true</noConfig>
    <versioning>semver</versioning>
</configuration>
```

**Maven Command-Line:**
```bash
mvn heylogs:check -Dheylogs.noConfig=true
```

**Use Cases:**
- **CI/CD pipelines** - Explicit configuration without hidden file dependencies
- **Testing** - Isolate test runs from project configuration
- **Debugging** - Determine if issues stem from config files
- **Override all** - Bypass entire config hierarchy when needed

---

[← Back to Features](features.md)

