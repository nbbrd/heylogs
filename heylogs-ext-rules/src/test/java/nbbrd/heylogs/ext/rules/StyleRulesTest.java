package nbbrd.heylogs.ext.rules;

import com.vladsch.flexmark.util.ast.Node;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.spi.RuleContext;
import nbbrd.heylogs.spi.RuleIssue;
import nbbrd.heylogs.spi.RuleSeverity;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static nbbrd.heylogs.ext.rules.RulesTestHelper.asBulletListItem;
import static nbbrd.heylogs.ext.rules.StyleRules.*;
import static org.assertj.core.api.Assertions.assertThat;
import static tests.heylogs.api.Sample.using;
import static tests.heylogs.spi.RuleAssert.assertRuleCompliance;

public class StyleRulesTest {

    @Test
    public void testCompliance() {
        assertRuleCompliance(new StyleRules.Batch());
    }

    @Test
    public void test() {
        Node sample = using("/Main.md");
        for (StyleRules rule : StyleRules.values()) {
            if (rule.getRuleSeverity() == RuleSeverity.OFF) continue;
            assertThat(Nodes.of(Node.class).descendants(sample).map(node -> rule.getRuleIssueOrNull(node, RuleContext.DEFAULT)).filter(Objects::nonNull))
                    .isEmpty();
        }
    }

    @Test
    public void testValidateConsistentSeparator() {
        assertThat(validateConsistentSeparator(using("/ErraticSeparator.md")))
                .isEqualTo(RuleIssue.builder().message("Expecting consistent version-date separator \\u002d, but also found: [\\u2013, \\u2014]").line(1).column(1).build());

        assertThat(validateConsistentSeparator(using("/NonDefaultSeparator.md")))
                .isEqualTo(NO_RULE_ISSUE);
    }

    @Test
    public void testValidateUniqueHeadings() {
        assertThat(validateUniqueHeadings(using("/NonUniqueHeadings.md")))
                .isEqualTo(RuleIssue.builder().message("Heading [1.1.0] - 2019-02-15 has 2 duplicate CHANGED entries").line(5).column(1).build());
    }

    @Test
    public void testValidateImbalancedBraces() {
        assertThat(validateImbalancedBraces(using("/ImbalancedBraces.md")))
                .isEqualTo(RuleIssue.builder().message("Imbalanced braces found in '- Danish translation from [@frederikspang](https://github.co\u2026'").line(9).column(1).build());
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

        assertThat(hasImbalancedBraces("`(`")).isFalse();
        assertThat(hasImbalancedBraces("`{`")).isFalse();
        assertThat(hasImbalancedBraces("`[`")).isFalse();
        assertThat(hasImbalancedBraces("`(]`")).isFalse();
        assertThat(hasImbalancedBraces("text `{unmatched` text")).isFalse();
        assertThat(hasImbalancedBraces("text `[code]` (balanced)")).isFalse();
        assertThat(hasImbalancedBraces("text `[code]` (unbalanced")).isTrue();
        assertThat(hasImbalancedBraces("(text `[ignore]` balanced)")).isFalse();
        assertThat(hasImbalancedBraces("(text `[ignore]` unbalanced")).isTrue();

        assertThat(hasImbalancedBraces("``(``")).isFalse();
        assertThat(hasImbalancedBraces("``[``")).isFalse();
        assertThat(hasImbalancedBraces("text ``[code]`` (balanced)")).isFalse();
        assertThat(hasImbalancedBraces("text ``[code]`` (unbalanced")).isTrue();
    }

    @Test
    public void testValidateColumnWidth() {
        assertThat(validateColumnWidth(asBulletListItem("- Short entry")))
                .describedAs("Short entry, no issue")
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateColumnWidth(asBulletListItem("- " + RulesTestHelper.repeat('a', 78))))
                .describedAs("Exactly 80 characters, no issue")
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateColumnWidth(asBulletListItem("- " + RulesTestHelper.repeat('a', 100))))
                .describedAs("Entry exceeds 80 characters")
                .extracting(RuleIssue::getMessage)
                .asString()
                .contains("Entry exceeds 80 characters");

        assertThat(validateColumnWidth(asBulletListItem("- Short text [link](https://very-long-url-that-exceeds-the-80-character-limit.com)")))
                .describedAs("Link starts before 80, no issue")
                .isEqualTo(NO_RULE_ISSUE);
    }
}

