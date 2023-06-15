package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import org.junit.jupiter.api.Test;

import static nbbrd.heylogs.Sample.asHeading;
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

        assertThat(Nodes.of(Heading.class).descendants(Sample.using("Main.md")).filter(TypeOfChange::isTypeOfChangeLevel).map(TypeOfChange::parse))
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
