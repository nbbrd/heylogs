package internal.heylogs.base;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.LinkNodeBase;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.VersioningConfig;
import nbbrd.heylogs.spi.RuleContext;
import nbbrd.heylogs.spi.RuleIssue;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import tests.heylogs.api.Sample;

import java.util.Objects;

import static internal.heylogs.base.BaseVersionings.REGEX_VERSIONING;
import static internal.heylogs.base.ExtendedRules.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;
import static tests.heylogs.api.Sample.using;
import static tests.heylogs.spi.RuleAssert.assertRuleCompliance;

public class ExtendedRulesTest {

    @Test
    public void testCompliance() {
        assertRuleCompliance(new ExtendedRules.Batch());
    }

    @Test
    public void test() {
        Node sample = Sample.using("/Main.md");
        for (ExtendedRules rule : ExtendedRules.values()) {
            Assertions.assertThat(Nodes.of(Node.class).descendants(sample).map(node -> rule.getRuleIssueOrNull(node, RuleContext.DEFAULT)).filter(Objects::nonNull))
                    .isEmpty();
        }
    }

    @Test
    public void testValidateHttps() {
        assertThat(Nodes.of(LinkNodeBase.class).descendants(using("/Main.md")))
                .map(ExtendedRules::validateHttps)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(LinkNodeBase.class).descendants(using("/NonHttps.md")))
                .map(ExtendedRules::validateHttps)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting HTTPS protocol").line(1).column(1).build(), atIndex(0))
                .contains(RuleIssue.builder().message("Expecting HTTPS protocol").line(2).column(7).build(), atIndex(1))
                .hasSize(2); // FIXME: should be 3
    }

    @Test
    public void testValidateConsistentSeparator() {
        assertThat(validateConsistentSeparator(using("/ErraticSeparator.md")))
                .isEqualTo(RuleIssue.builder().message("Expecting consistent version-date separator \\u002d, found [\\u002d, \\u2013, \\u2014]").line(1).column(1).build());

        assertThat(validateConsistentSeparator(using("/NonDefaultSeparator.md")))
                .isEqualTo(NO_RULE_ISSUE);
    }

    @Test
    public void testValidateUniqueHeadings() {
        assertThat(validateUniqueHeadings(using("/NonUniqueHeadings.md")))
                .isEqualTo(RuleIssue.builder().message("Heading [1.1.0] - 2019-02-15 has 2 duplicate CHANGED entries").line(5).column(1).build());
    }

    @Test
    public void testValidateNoEmptyGroup() {
        assertThat(validateNoEmptyGroup(using("/NoEmptyGroup.md")))
                .isEqualTo(RuleIssue.builder().message("Heading [1.1.0] - 2019-02-15 has no entries for CHANGED").line(7).column(1).build());
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
    public void testValidateImbalancedBraces() {
        assertThat(validateImbalancedBraces(using("/ImbalancedBraces.md")))
                .isEqualTo(RuleIssue.builder().message("Imbalanced braces found in '- Danish translation from [@frederikspang](https://github.com/frederikspang)].'").line(9).column(1).build());
    }

    @Test
    public void testHasImbalancedBraces() {
        assertThat(hasImbalancedBraces("")).isFalse();
        assertThat(hasImbalancedBraces("()")).isFalse();
        assertThat(hasImbalancedBraces("{}")).isFalse();
        assertThat(hasImbalancedBraces("[]")).isFalse();
        assertThat(hasImbalancedBraces("({}[])")).isFalse();
        assertThat(hasImbalancedBraces("[{()}]")).isFalse();
        assertThat(hasImbalancedBraces("(]")).isTrue();
        assertThat(hasImbalancedBraces("{[}]")).isTrue();
        assertThat(hasImbalancedBraces("[(])")).isTrue();
        assertThat(hasImbalancedBraces("{(})")).isTrue();
        assertThat(hasImbalancedBraces("(()")).isTrue();
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
}
