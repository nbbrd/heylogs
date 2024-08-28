package internal.heylogs;

import tests.heylogs.api.Sample;
import com.vladsch.flexmark.ast.LinkNodeBase;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleIssue;
import nbbrd.heylogs.spi.RuleLoader;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static tests.heylogs.api.Sample.using;
import static internal.heylogs.ExtendedRules.NO_RULE_ISSUE;
import static internal.heylogs.ExtendedRules.validateConsistentSeparator;
import static nbbrd.heylogs.Nodes.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;

public class ExtendedRulesTest {

    @Test
    public void testIdPattern() {
        assertThat(ExtendedRules.values())
                .extracting(Rule::getRuleId)
                .allMatch(RuleLoader.ID_PATTERN.asPredicate());
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
        assertThat(of(LinkNodeBase.class).descendants(using("/Main.md")))
                .map(ExtendedRules::validateHttps)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(of(LinkNodeBase.class).descendants(using("/NonHttps.md")))
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
}
