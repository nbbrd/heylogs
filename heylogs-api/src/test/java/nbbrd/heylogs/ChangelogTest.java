package nbbrd.heylogs;

import tests.heylogs.api.Sample;
import com.vladsch.flexmark.ast.Heading;
import org.junit.jupiter.api.Test;

import static tests.heylogs.api.Sample.using;
import static nbbrd.heylogs.Changelog.parse;
import static tests.heylogs.api.Sample.asHeading;
import static org.assertj.core.api.Assertions.*;

public class ChangelogTest {

    @Test
    public void testParse() {
        //noinspection DataFlowIssue
        assertThatNullPointerException()
                .isThrownBy(() -> parse(null));

        assertThat(parse(asHeading("# Changelog")))
                .isEqualTo(Changelog.INSTANCE);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(asHeading("## Changelog")))
                .withMessageContaining("Invalid heading level");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(asHeading("# hello")))
                .withMessageContaining("Invalid text");

        assertThat(Nodes.of(Heading.class).descendants(using("/Main.md")).filter(Changelog::isChangelogLevel).map(Changelog::parse))
                .hasSize(1)
                .contains(Changelog.INSTANCE, atIndex(0));
    }

    @Test
    public void testToHeading() {
        assertThat(Changelog.INSTANCE.toHeading())
                .extracting(Sample::asText, STRING)
                .isEqualTo("# Changelog");
    }
}
