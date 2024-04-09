package internal.heylogs.semver;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.heylogs.spi.RuleIssue;
import nbbrd.heylogs.spi.RuleLoader;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static _test.Sample.using;
import static nbbrd.heylogs.Nodes.of;
import static org.assertj.core.api.Assertions.assertThat;

public class SemVerRuleTest {

    @Test
    public void testIdPattern() {
        assertThat(new SemVerRule().getRuleId())
                .matches(RuleLoader.ID_PATTERN);
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
