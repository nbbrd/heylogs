package nbbrd.heylogs.ext.semver;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.heylogs.spi.RuleIssue;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static nbbrd.heylogs.Nodes.of;
import static org.assertj.core.api.Assertions.assertThat;
import static tests.heylogs.api.Sample.using;
import static tests.heylogs.spi.RuleAssert.assertRuleCompliance;

public class SemVerRuleTest {

    @Test
    public void testCompliance() {
        assertRuleCompliance(new SemVerRule());
    }

    @Test
    public void testSample() {
        SemVerRule x = new SemVerRule();

        assertThat(of(Node.class).descendants(using("/Main.md")))
                .map(x::getRuleIssueOrNull)
                .filteredOn(Objects::nonNull)
                .isEmpty();
    }

    @Test
    public void testValidateSemVer() {
        SemVerRule x = new SemVerRule();

        assertThat(of(Heading.class).descendants(using("/Main.md")))
                .map(x::validateSemVer)
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(of(Heading.class).descendants(using("/Empty.md")))
                .map(x::validateSemVer)
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(of(Heading.class).descendants(using("/InvalidSemver.md")))
                .map(x::validateSemVer)
                .filteredOn(Objects::nonNull)
                .hasSize(1)
                .contains(RuleIssue.builder().message("Invalid semver format: '.1.0'").line(4).column(1).build());
    }
}
