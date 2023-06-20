package nbbrd.heylogs;

import _test.Sample;
import com.vladsch.flexmark.ast.Heading;
import org.junit.jupiter.api.Test;

import static _test.Sample.asHeading;
import static _test.Sample.using;
import static nbbrd.heylogs.TypeOfChange.parse;
import static org.assertj.core.api.Assertions.*;

public class TypeOfChangeTest {

    @Test
    public void testParse() {
        //noinspection DataFlowIssue
        assertThatNullPointerException()
                .isThrownBy(() -> parse(null));

        assertThat(parse(asHeading("### Added")))
                .isEqualTo(TypeOfChange.ADDED);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(asHeading("## Added")))
                .withMessageContaining("Invalid heading level");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(asHeading("#### Added")))
                .withMessageContaining("Invalid heading level");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(asHeading("### hello")))
                .withMessageContaining("Cannot parse");

        assertThat(Nodes.of(Heading.class).descendants(using("/Main.md")).filter(TypeOfChange::isTypeOfChangeLevel).map(TypeOfChange::parse))
                .hasSize(24)
                .contains(TypeOfChange.ADDED, atIndex(0));
    }

    @Test
    public void testToHeading() {
        assertThat(TypeOfChange.ADDED.toHeading())
                .extracting(Sample::asText, STRING)
                .isEqualTo("### Added");
    }
}
