package nbbrd.heylogs.ext.semver;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.spi.RuleIssue;
import org.junit.jupiter.api.Test;

import java.util.Objects;

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

        Config withoutSemver = Config.DEFAULT;
        Config withSemver = Config.builder().versioningId(SemVer.ID).build();

        assertThat(Nodes.of(Node.class).descendants(using("/Main.md")))
                .map(node -> x.getRuleIssueOrNull(node, withoutSemver))
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Node.class).descendants(using("/Main.md")))
                .map(node -> x.getRuleIssueOrNull(node, withSemver))
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Heading.class).descendants(using("/Empty.md")))
                .map(node -> x.getRuleIssueOrNull(node, withoutSemver))
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Heading.class).descendants(using("/Empty.md")))
                .map(node -> x.getRuleIssueOrNull(node, withSemver))
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Heading.class).descendants(using("/InvalidSemver.md")))
                .map(node -> x.getRuleIssueOrNull(node, withoutSemver))
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Heading.class).descendants(using("/InvalidSemver.md")))
                .map(node -> x.getRuleIssueOrNull(node, withSemver))
                .filteredOn(Objects::nonNull)
                .hasSize(1)
                .contains(RuleIssue.builder().message("Invalid semver format: '.1.0'").line(4).column(1).build());
    }
}
