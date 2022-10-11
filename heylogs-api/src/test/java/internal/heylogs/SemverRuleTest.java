package internal.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Node;
import internal.heylogs.SemverRule;
import nbbrd.heylogs.Failure;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static nbbrd.heylogs.Nodes.of;
import static nbbrd.heylogs.Sample.using;
import static org.assertj.core.api.Assertions.assertThat;

public class SemverRuleTest {

    @Test
    public void testSample() {
        SemverRule x = new SemverRule();

        assertThat(of(Node.class).descendants(using("Main.md")))
                .map(x::validate)
                .filteredOn(Objects::nonNull)
                .isEmpty();
    }

    @Test
    public void testValidateSemVer() {
        SemverRule x = new SemverRule();

        assertThat(of(Heading.class).descendants(using("Main.md")))
                .map(x::validateSemVer)
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(of(Heading.class).descendants(using("Empty.md")))
                .map(x::validateSemVer)
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(of(Heading.class).descendants(using("InvalidSemver.md")))
                .map(x::validateSemVer)
                .filteredOn(Objects::nonNull)
                .hasSize(1)
                .contains(Failure.of(x, "Invalid semver format: '.1.0'", 2, 1));
    }
}
