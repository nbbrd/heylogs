---
name: create-rule
description: >
  Use this skill when asked to add, create, or implement a new validation rule
  in the Heylogs project. It covers picking the right enum, adding the constant,
  the validate method, the test, the resource file (if needed), and the
  documentation row.
---

Add a new validation rule to `heylogs-ext-rules` by following these steps in order. Execute all steps without asking for confirmation.

## Step 0 — Choose the right enum

Rules live in three categorized enums under `heylogs-ext-rules/src/main/java/nbbrd/heylogs/ext/rules/`:

| Enum           | File                | Category                             | Typical node type                        |
|----------------|---------------------|--------------------------------------|------------------------------------------|
| `ReleaseRules` | `ReleaseRules.java` | Release structure and versioning     | `Document`, `Heading`                    |
| `LinkRules`    | `LinkRules.java`    | Link validation and reference checks | `Link`, `LinkNodeBase`, `BulletListItem` |
| `StyleRules`   | `StyleRules.java`   | Formatting and style conventions     | `Document`, `BulletListItem`             |

Pick the enum whose category best matches the new rule, then use that file for Steps 1–3.

> **Note**: Rules for core changelog structure (changelog heading, H2 version format, type-of-change headings, date display, version ordering) belong to `GuidingPrinciples` in `heylogs-api` and are not part of this skill.

## Step 1 — Add the enum constant

**File**: `heylogs-ext-rules/src/main/java/nbbrd/heylogs/ext/rules/<ChosenEnum>.java`

**Action**: Use `replace_string_in_file` to add a new constant BEFORE the final `;` of the enum (after the last existing constant). The rule ID is derived automatically from the constant name (`MY_RULE` → `my-rule`).

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

| Node type               | What it covers                     | Common pattern                                       |
|-------------------------|------------------------------------|------------------------------------------------------|
| `Document`              | Whole-document checks              | Use `ChangelogHeading.root(doc)` helper              |
| `Heading`               | Version or type-of-change headings | Check `Version.isVersionLevel(heading)`              |
| `BulletListItem`        | Changelog list entries             | Use `item.getChars().trim().toString()`              |
| `Link` / `LinkNodeBase` | Inline links                       | Parse URL with `Parser.onURL().parse(link.getUrl())` |

## Step 2 — Add the static validate method

**File**: Same as Step 1

**Action**: Use `replace_string_in_file` to add a `@VisibleForTesting` static method AFTER the last existing `validate*` method (before any inner class or `Batch` class at the end).

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
- For `Document` rules, delegate to a private helper: `ChangelogHeading.root(doc).map(MyEnum::validateHelper).orElse(NO_RULE_ISSUE)`
- If the new method needs `truncate()`, call `RulesUtil.truncate(text, maxLength)` (package-private helper in the same package)

## Step 3 — Add the test

**File**: `heylogs-ext-rules/src/test/java/nbbrd/heylogs/ext/rules/<ChosenEnum>Test.java`

**Action**: Use `replace_string_in_file` to add a test method AFTER the last existing test method (before the closing `}`).

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
- Import the static methods at the top: `import static nbbrd.heylogs.ext.rules.<ChosenEnum>.*;`
- Use `.isNull()` instead of `.isEqualTo(NO_RULE_ISSUE)` (they are equivalent but `isNull` is cleaner)
- Use `.describedAs("...")` for each assertion to document test intent
- For `Document`-level rules, load a Markdown fixture with `using("/MyTestFile.md")` (see Step 3a)
- Helper methods available: `asBulletListItem(String)` and `asLink(String)` are in `RulesTestHelper`; `asHeading(String)` and `using(String)` are in `tests.heylogs.api.Sample`

### Step 3a — Add test resource (only for Document-level rules)

If the test uses `using("/MyTestFile.md")`, the `.md` file must exist in **both**:

1. `heylogs-api/src/test/resources/MyTestFile.md` — original source of truth
2. `heylogs-ext-rules/src/test/resources/MyTestFile.md` — local copy required for the ext-rules test classpath

Create both files with the minimal changelog content needed to trigger the rule.

> **Why two copies?** The `heylogs-api` test-jar only packages `.class` files, not test resources. Each ext module that needs a resource must carry its own copy under `src/test/resources/`.

## Step 4 — Update documentation

**File**: `docs/feature-rules.md`

**Action**: Use `replace_string_in_file` to add a row to the table in **alphabetical order** by rule ID (kebab-case). Use `rules` as the module name.

**Row format**:
```markdown
| `rules` | [`my-rule`](#my-rule) | My rule | `ERROR` |
```

**Severity values**: `ERROR` (default), `WARN`, or `OFF`

Also add a reference section entry at the bottom of the file:

```markdown
### `my-rule`
Description of what the rule checks and when it fires.
```

## Post-implementation checks

**Validation**: After all edits, call `get_errors` on both modified Java files to verify no compilation errors.

**Files to validate**:
- `heylogs-ext-rules/src/main/java/nbbrd/heylogs/ext/rules/<ChosenEnum>.java`
- `heylogs-ext-rules/src/test/java/nbbrd/heylogs/ext/rules/<ChosenEnum>Test.java`

## Testing (optional)

```shell
# Fast test of the changed module (from workspace root)
mvn test -pl heylogs-ext-rules -am -Pyolo

# Run only the new test method
mvn test -pl heylogs-ext-rules -am -Pyolo -Dtest=<ChosenEnum>Test#testValidateMyRule

# Full build with all checks
mvn clean install
```

## Constraints

- Rule ID must match `ServiceId.KEBAB_CASE` (enforced at runtime by `RuleSupport`).
- Default severity is `ERROR`; only override when `WARN` or `OFF` is needed.
- The `test()` method in each `*Test` class automatically skips rules with `OFF` severity.
- Never reference `internal.*` packages from public API classes. The three rule enums are in the `nbbrd.heylogs.ext.rules` public package and may only access `internal.heylogs.*` directly (not `internal.heylogs.base.*`, `internal.heylogs.git.*`, etc.) — enforced by `ArchitectureTest`.
- `getRuleModuleId()` must return `"rules"` (already inherited from the enum body; do not change it).
- Target Java 8; avoid `String.repeat()`, `var`, `Stream.toList()`, `List.of()`.
- Use `trim()` before checking text content of `BulletListItem`.
- All common imports are already present in each enum; only add new ones if strictly required.
