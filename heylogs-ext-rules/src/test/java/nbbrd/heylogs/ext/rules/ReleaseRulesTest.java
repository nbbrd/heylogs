package nbbrd.heylogs.ext.rules;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.VersioningConfig;
import nbbrd.heylogs.spi.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

import static internal.heylogs.base.BaseVersionings.REGEX_VERSIONING;
import static nbbrd.heylogs.ext.rules.ReleaseRules.*;
import static org.assertj.core.api.Assertions.assertThat;
import static tests.heylogs.api.Sample.asHeading;
import static tests.heylogs.api.Sample.using;
import static tests.heylogs.spi.RuleAssert.assertRuleCompliance;

public class ReleaseRulesTest {

    @Test
    public void testCompliance() {
        assertRuleCompliance(new ReleaseRules.Batch());
    }

    @Test
    public void test() {
        Node sample = using("/Main.md");
        for (ReleaseRules rule : ReleaseRules.values()) {
            if (rule.getRuleSeverity() == RuleSeverity.OFF) continue;
            assertThat(Nodes.of(Node.class).descendants(sample).map(node -> rule.getRuleIssueOrNull(node, RuleContext.DEFAULT)).filter(Objects::nonNull))
                    .isEmpty();
        }
    }

    @Test
    public void testValidateNoEmptyGroup() {
        assertThat(validateNoEmptyGroup(using("/NoEmptyGroup.md")))
                .isEqualTo(RuleIssue.builder().message("Heading [1.1.0] - 2019-02-15 has no entries for CHANGED").line(17).column(1).build());
    }

    @Test
    public void testValidateNoEmptyRelease() {
        assertThat(validateNoEmptyRelease(using("/NoEmptyRelease.md")))
                .isEqualTo(RuleIssue.builder().message("Heading [1.1.0] - 2019-02-15 has no entries").line(5).column(1).build());
    }

    @Test
    public void testValidateUniqueRelease() {
        assertThat(validateUniqueRelease(using("/UniqueRelease.md")))
                .isEqualTo(RuleIssue.builder().message("Release 1.1.0 has 2 duplicates").line(5).column(1).build());
    }

    @Test
    public void testValidateDuplicateItems() {
        assertThat(validateDuplicateItems(using("/DuplicateItems.md")))
                .extracting(RuleIssue::getMessage)
                .asString()
                .contains("Duplicate item found in version 1.1.0 across ADDED and CHANGED")
                .contains("- Danish translation from [@frederikspang](https://github.co\u2026")
                .contains("appears 2 times");

        assertThat(validateDuplicateItems(using("/DuplicateItemsAcrossVersions.md")))
                .extracting(RuleIssue::getMessage)
                .asString()
                .contains("Duplicate item found across versions 1.1.0 (ADDED) and 1.0.0 (FIXED)")
                .contains("- Danish translation from [@frederikspang](https://github.co\u2026")
                .contains("appears 2 times");
    }

    @Test
    public void testValidateVersioningFormat() {
        RuleContext withoutSemver = RuleContext.DEFAULT;
        RuleContext withSemver = RuleContext
                .builder()
                .config(Config
                        .builder()
                        .versioning(VersioningConfig.parse("regex:^\\d+\\.\\d+\\.\\d+$"))
                        .build())
                .versioning(REGEX_VERSIONING)
                .build();

        assertThat(Nodes.of(Heading.class).descendants(using("/InvalidSemver.md")))
                .map(node -> VERSIONING_FORMAT.getRuleIssueOrNull(node, withoutSemver))
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Heading.class).descendants(using("/InvalidSemver.md")))
                .map(node -> VERSIONING_FORMAT.getRuleIssueOrNull(node, withSemver))
                .filteredOn(Objects::nonNull)
                .hasSize(1)
                .contains(RuleIssue.builder().message("Invalid reference '.1.0' when using versioning 'regex:^\\d+\\.\\d+\\.\\d+$'").line(4).column(1).build());
    }

    @Test
    public void testValidateReleaseDate() {
        assertThat(validateReleaseDate(asHeading("## [Unreleased]"), RuleContext.DEFAULT))
                .isEqualTo(NO_RULE_ISSUE);

        LocalDate now = LocalDate.now(ZoneId.systemDefault());

        assertThat(validateReleaseDate(asHeading("## [1.0.0] - " + now), RuleContext.DEFAULT))
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateReleaseDate(asHeading("## [1.0.0] - " + now.minusDays(1)), RuleContext.DEFAULT))
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateReleaseDate(asHeading("## [1.0.0] - " + now.plusDays(1)), RuleContext.DEFAULT))
                .isEqualTo(RuleIssue.builder().message("Release date " + now.plusDays(1) + " is in the future").line(1).column(1).build());

        assertThat(validateReleaseDate(asHeading("### [1.0.0] - " + now.plusDays(1)), RuleContext.DEFAULT))
                .isEqualTo(NO_RULE_ISSUE);
    }

    @Test
    public void testValidateNoVersionRegression() {
        RuleContext contextWithoutVersioning = RuleContext.DEFAULT;

        assertThat(validateNoVersionRegression(using("/Main.md"), contextWithoutVersioning))
                .describedAs("No versioning configured, no issue")
                .isEqualTo(NO_RULE_ISSUE);

        Versioning numericVersioning = VersioningSupport
                .builder()
                .id("test-numeric")
                .name("Test Numeric")
                .urlOf("http://example.com")
                .moduleId("test")
                .validator(arg -> null)
                .predicate(arg -> text -> text.toString().matches("\\d+\\.\\d+\\.\\d+"))
                .comparator(arg -> (a, b) -> {
                    String[] aParts = a.toString().split("\\.");
                    String[] bParts = b.toString().split("\\.");
                    if (aParts.length != 3 || bParts.length != 3) return 0;

                    for (int i = 0; i < 3; i++) {
                        int cmp = Integer.compare(Integer.parseInt(aParts[i]), Integer.parseInt(bParts[i]));
                        if (cmp != 0) return cmp;
                    }
                    return 0;
                })
                .familyMapper(arg -> version -> {
                    String[] parts = version.toString().split("\\.");
                    if (parts.length >= 2) {
                        return parts[0] + "." + parts[1];
                    }
                    return null;
                })
                .build();

        Config config = Config.builder().versioningOf("test-numeric").build();
        RuleContext contextWithVersioning = RuleContext.builder()
                .config(config)
                .versioning(numericVersioning)
                .build();

        assertThat(validateNoVersionRegression(using("/Main.md"), contextWithVersioning))
                .describedAs("Valid changelog with versioning, no issue")
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateNoVersionRegression(using("/VersionRegression.md"), contextWithVersioning))
                .describedAs("Version regression detected")
                .isNotNull()
                .extracting(RuleIssue::getMessage)
                .asString()
                .contains("2.4.0")
                .contains("2.4.1");

        assertThat(validateNoVersionRegression(using("/DuplicateVersions.md"), contextWithVersioning))
                .describedAs("Duplicate versions treated as same family - no regression within duplicates")
                .isEqualTo(NO_RULE_ISSUE);
    }
}

