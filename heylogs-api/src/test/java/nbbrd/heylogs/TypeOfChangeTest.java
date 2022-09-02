package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class TypeOfChangeTest {

    @Test
    public void testParseHeading() {
        assertThat(parsingHeading("### Added"))
                .isEqualTo(TypeOfChange.ADDED);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parsingHeading("## Added"))
                .withMessageContaining("Invalid heading level");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parsingHeading("#### Added"))
                .withMessageContaining("Invalid heading level");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parsingHeading("### hello"))
                .withMessageContaining("Cannot parse");

        assertThat(Nodes.of(Heading.class).descendants(Sample.using("Main.md")).filter(TypeOfChange::isTypeOfChangeLevel).map(TypeOfChange::parse))
                .hasSize(24)
                .contains(TypeOfChange.ADDED, atIndex(0));
    }

    @Test
    public void testFormatHeading() {
        assertThat(TypeOfChange.ADDED.toHeading())
                .extracting(Sample::asText)
                .asString()
                .isEqualTo("### Added");
    }

    @NotNull
    private TypeOfChange parsingHeading(String text) {
        return TypeOfChange.parse(Sample.asHeading(text));
    }
}
