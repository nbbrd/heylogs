package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

public class ChangelogTest {

    @Test
    public void testParseHeading() {
        assertThat(parsingHeading("# Changelog"))
                .isEqualTo(Changelog.INSTANCE);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parsingHeading("## Changelog"))
                .withMessageContaining("Invalid heading level");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parsingHeading("# hello"))
                .withMessageContaining("Invalid text");

        assertThat(Nodes.of(Heading.class).descendants(Sample.using("Main.md")).filter(Changelog::isChangelogLevel).map(Changelog::parse))
                .hasSize(1)
                .contains(Changelog.INSTANCE, atIndex(0));
    }

    @Test
    public void testFormatHeading() {
        assertThat(Changelog.INSTANCE.toHeading())
                .extracting(Sample::asText)
                .asString()
                .isEqualTo("# Changelog");
    }

    @NotNull
    private Changelog parsingHeading(String text) {
        return Changelog.parse(Sample.asHeading(text));
    }
}
