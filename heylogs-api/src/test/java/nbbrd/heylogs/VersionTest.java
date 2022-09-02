package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

public class VersionTest {

    @Test
    public void testParseHeading() {
        assertThat(parsingHeading("## [Unreleased]"))
                .isEqualTo(new Version("Unreleased", LocalDate.MAX));

        assertThat(parsingHeading("## [Unreleased ]"))
                .isEqualTo(new Version("Unreleased", LocalDate.MAX));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parsingHeading("## [Unreleased] - 2019-02-15"))
                .withMessageContaining("Unexpected additional part");

        assertThat(parsingHeading("## [1.1.0] - 2019-02-15"))
                .isEqualTo(new Version("1.1.0", d20190215));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parsingHeading("# [1.1.0] - 2019-02-15"))
                .withMessageContaining("Invalid heading level");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parsingHeading("### [1.1.0] - 2019-02-15"))
                .withMessageContaining("Invalid heading level");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parsingHeading("## Unreleased"))
                .withMessageContaining("Missing ref link");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parsingHeading("## 1.1.0 - 2019-02-15"))
                .withMessageContaining("Missing ref link");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parsingHeading("## [1.1.0](https://localhost) - 2019-02-15"))
                .withMessageContaining("Missing ref link");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> Version.parse(Sample.asHeading("## [1.1.0] - ")))
                .withMessageContaining("Invalid date");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parsingHeading("## [1.1.0] - 2019-02"))
                .withMessageContaining("Invalid date");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parsingHeading("## - 2019-02-15"))
                .withMessageContaining("Missing ref link");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parsingHeading("## [1.1.0] - 2019-02-15 [hello]"))
                .withMessageContaining("Unexpected additional part");

        assertThat(Nodes.of(Heading.class).descendants(Sample.using("Main.md")).filter(Version::isVersionLevel).map(Version::parse))
                .hasSize(14)
                .contains(new Version("Unreleased", LocalDate.MAX), atIndex(0))
                .contains(new Version("1.1.0", d20190215), atIndex(1));
    }

    @Test
    public void testFormatHeading() {
        assertThat(new Version("Unreleased", LocalDate.MAX).toHeading())
                .extracting(Sample::asText)
                .asString()
                .isEqualTo("## [Unreleased]");

        assertThat(new Version("1.1.0", d20190215).toHeading())
                .extracting(Sample::asText)
                .asString()
                .isEqualTo("## [1.1.0] - 2019-02-15");
    }

    @NotNull
    private Version parsingHeading(String text) {
        return Version.parse(Sample.asHeading(text));
    }

    private final LocalDate d20190215 = LocalDate.parse("2019-02-15");
}
