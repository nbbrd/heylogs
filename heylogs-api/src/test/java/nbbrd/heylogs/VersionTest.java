package nbbrd.heylogs;

import _test.Sample;
import com.vladsch.flexmark.ast.Heading;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static _test.Sample.asHeading;
import static _test.Sample.using;
import static nbbrd.heylogs.Version.parse;
import static org.assertj.core.api.Assertions.*;

public class VersionTest {

    @Test
    public void testParse() {
        //noinspection DataFlowIssue
        assertThatNullPointerException()
                .isThrownBy(() -> parse(null));

        assertThat(parse(asHeading("## [Unreleased]")))
                .isEqualTo(Version.of("Unreleased", LocalDate.MAX));

        assertThat(parse(asHeading("## [Unreleased ]")))
                .isEqualTo(Version.of("Unreleased", LocalDate.MAX));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(asHeading("## [Unreleased] - 2019-02-15")))
                .withMessageContaining("Unexpected additional part");

        assertThat(parse(asHeading("## [1.1.0] - 2019-02-15")))
                .isEqualTo(Version.of("1.1.0", d20190215));

        // Unicode en dash as separator
        assertThat(parse(asHeading("## [1.1.0] – 2019-02-15")))
                .isEqualTo(Version.of("1.1.0", d20190215));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(asHeading("# [1.1.0] - 2019-02-15")))
                .withMessageContaining("Invalid heading level");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(asHeading("### [1.1.0] - 2019-02-15")))
                .withMessageContaining("Invalid heading level");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(asHeading("##")))
                .withMessageContaining("Missing ref part");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(asHeading("## Unreleased")))
                .withMessageContaining("Missing ref link");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(asHeading("## 1.1.0 - 2019-02-15")))
                .withMessageContaining("Missing ref link");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(asHeading("## [1.1.0](https://localhost) - 2019-02-15")))
                .withMessageContaining("Missing ref link");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(asHeading("## [1.1.0] 2019-02-15")))
                .withMessageContaining("Missing date prefix");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(asHeading("## [1.1.0] - ")))
                .withMessageContaining("Invalid date format");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(asHeading("## [1.1.0] - 2019-02")))
                .withMessageContaining("Invalid date format");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(asHeading("## - 2019-02-15")))
                .withMessageContaining("Missing ref link");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(asHeading("## [1.1.0] - 2019-02-15 [hello]")))
                .withMessageContaining("Unexpected additional part");

        assertThat(Nodes.of(Heading.class).descendants(using("/Main.md")).filter(Version::isVersionLevel).map(Version::parse))
                .hasSize(14)
                .contains(Version.of("Unreleased", LocalDate.MAX), atIndex(0))
                .contains(Version.of("1.1.0", d20190215), atIndex(1));
    }

    @Test
    public void testToHeading() {
        assertThat(Version.of("Unreleased", LocalDate.MAX).toHeading())
                .extracting(Sample::asText)
                .asString()
                .isEqualTo("## [Unreleased]");

        assertThat(Version.of("1.1.0", d20190215).toHeading())
                .extracting(Sample::asText, STRING)
                .isEqualTo("## [1.1.0] - 2019-02-15");
    }

    private final LocalDate d20190215 = LocalDate.parse("2019-02-15");
}
