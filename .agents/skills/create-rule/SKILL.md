---
name: create-rule
description: >
  Use this skill when asked to add, create, or implement a new validation rule
  in the Heylogs project. It covers adding the enum constant, the validate
  method, the test, the rule count update, and the documentation row.
---

Add a new validation rule to `ExtendedRules` in `heylogs-api` by following these steps in order. Execute all steps without asking for confirmation.

## Step 1 — Add the enum constant

**File**: `heylogs-api/src/main/java/internal/heylogs/base/ExtendedRules.java`

**Action**: Use `replace_string_in_file` to add a new constant BEFORE the final `;` of the enum (after `COLUMN_WIDTH`). The rule ID is derived automatically from the constant name (`MY_RULE` → `my-rule`).

**Template**:
```java
MY_RULE {
    @Override
    public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull RuleContext context) {
        return node instanceof BulletListItem ? validateMyRule((BulletListItem) node) : NO_RULE_ISSUE;
    }

    @Override
    public @NonNull String getRuleName() {
        return "My rule";
    }

    // Only add this override when severity is not ERROR:
    @Override
    public @NonNull RuleSeverity getRuleSeverity() {
        return RuleSeverity.WARN; // or OFF
    }
},
```

**Node type selection**:

| Node type | What it covers | Common pattern |
|-----------|---------------|----------------|
| `Document` | Whole-document checks | Use `ChangelogHeading.root(doc)` helper |
| `Heading` | Version or type-of-change headings | Check `Version.isVersionLevel(heading)` |
| `BulletListItem` | Changelog list entries | Use `item.getChars().trim().toString()` |
| `Link` / `LinkNodeBase` | Inline links | Parse URL with `Parser.onURL().parse(link.getUrl())` |

## Step 2 — Add the static validate method

**File**: Same as Step 1

**Action**: Use `replace_string_in_file` to add a `@VisibleForTesting` static method AFTER the last validate method (before the `ItemLocation` inner class if present).

**Template for BulletListItem rules**:
```java
@VisibleForTesting
static @Nullable RuleIssue validateMyRule(@NonNull BulletListItem item) {
    String text = item.getChars().trim().toString();
    if (text.isEmpty()) return NO_RULE_ISSUE;
    
    // Early return NO_RULE_ISSUE for valid cases
    if (someCondition) return NO_RULE_ISSUE;
    
    // Return issue for invalid cases
    return RuleIssue
            .builder()
            .message("Descriptive error message")
            .location(item)
            .build();
}
```

**Best practices**:
- Always use `.trim()` when checking `BulletListItem` text content
- Use early returns with `NO_RULE_ISSUE` for valid/skip cases
- Use `Character` utility methods for character checks (e.g., `Character.isLetter()`, `Character.isUpperCase()`)
- Use `String.format(ROOT, "...")` for parameterized messages
- For Document rules, delegate to helper: `ChangelogHeading.root(doc).map(this::validateHelper).orElse(NO_RULE_ISSUE)`

## Step 3 — Add the test

**File**: `heylogs-api/src/test/java/internal/heylogs/base/ExtendedRulesTest.java`

**Action**: Use `replace_string_in_file` to add test method AFTER the last existing test method (before the `repeat()` helper if present).

**Template**:
```java
@Test
public void testValidateMyRule() {
    assertThat(validateMyRule(asBulletListItem("- Valid entry")))
            .describedAs("valid case")
            .isNull();

    assertThat(validateMyRule(asBulletListItem("- Invalid entry")))
            .describedAs("invalid case")
            .isEqualTo(RuleIssue.builder().message("Expected message").line(1).column(1).build());
}
```

**Tips**:
- Use `.isNull()` instead of `.isEqualTo(NO_RULE_ISSUE)` (they are equivalent but isNull is cleaner)
- Use `.describedAs("...")` for each assertion to document test intent
- For Document rules, load a Markdown fixture with `using("/MyTestFile.md")` and place it under `heylogs-api/src/test/resources/`
- Helper methods available: `asBulletListItem(String)`, `asLink(String)`, `asHeading(String)`, `using(String)`

## Step 4 — Update rule count

**File**: `heylogs-api/src/test/java/nbbrd/heylogs/HeylogsTest.java`

**Action**: Use `replace_string_in_file` to increment the `hasSize` value by 1 in the `testBaseProviders()` method.

**Current value**: Check the current size first, then increment by 1.

**Example**:
```java
assertThat(x.getRules())
        .hasSize(24); // was 23, now 24
```

## Step 5 — Update documentation

**File**: `docs/feature-rules.md`

**Action**: Use `replace_string_in_file` to add a row to the table in **alphabetical order** by rule ID (kebab-case).

**Row format**:
```markdown
| `api`  | `my-rule` | My rule | `ERROR` |
```

**Severity values**: `ERROR` (default), `WARN`, or `OFF`

## Post-implementation checks

**Validation**: After all edits, call `get_errors` on both modified Java files to verify no compilation errors.

**Files to validate**:
- `heylogs-api/src/main/java/internal/heylogs/base/ExtendedRules.java`
- `heylogs-api/src/test/java/internal/heylogs/base/ExtendedRulesTest.java`

## Testing (optional)

```shell
# Fast test of the changed module (from workspace root)
mvn test -pl heylogs-api -Pyolo

# Run only the new test method
mvn test -pl heylogs-api -Pyolo -Dtest=ExtendedRulesTest#testValidateMyRule

# Full build with all checks
mvn clean install
```

## Constraints

- Rule ID must match `ServiceId.KEBAB_CASE` (enforced at runtime by `RuleSupport`).
- Default severity is `ERROR`; only override when `WARN` or `OFF` is needed.
- The `test()` method in `ExtendedRulesTest` automatically skips rules with `OFF` severity.
- Never reference `internal.*` packages from public API classes.
- Target Java 8; avoid `String.repeat()`, `var`, `Stream.toList()`, `List.of()`.
- Use `trim()` before checking text content of `BulletListItem`.
- All imports are already present; no new imports needed for basic rules.
