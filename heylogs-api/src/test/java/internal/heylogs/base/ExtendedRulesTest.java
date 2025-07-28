package internal.heylogs.base;

import com.vladsch.flexmark.ast.LinkNodeBase;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.spi.RuleIssue;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import tests.heylogs.api.Sample;

import java.util.Objects;

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
            Assertions.assertThat(Nodes.of(Node.class).descendants(sample).map(rule::getRuleIssueOrNull).filter(Objects::nonNull))
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
}
