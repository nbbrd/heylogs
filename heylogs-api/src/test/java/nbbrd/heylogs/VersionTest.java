package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import org.junit.jupiter.api.Test;
import tests.heylogs.api.Sample;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.Version.*;
import static org.assertj.core.api.Assertions.*;
import static tests.heylogs.api.Sample.asHeading;
import static tests.heylogs.api.Sample.using;

public class VersionTest {

    @Test
    public void testParse() {
        //noinspection DataFlowIssue
        assertThatNullPointerException()
                .isThrownBy(() -> parse(null));

        assertThat(parse(asHeading("## [Unreleased]")))
                .isEqualTo(Version.of("Unreleased", null, HYPHEN, LocalDate.MAX));

        assertThat(parse(asHeading("## [Unreleased ]")))
                .isEqualTo(Version.of("Unreleased", null, HYPHEN, LocalDate.MAX));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(asHeading("## [Unreleased] - 2019-02-15")))
                .withMessageContaining("Unexpected additional part");

        assertThat(parse(asHeading("## [1.1.0] - 2019-02-15")))
                .isEqualTo(Version.of("1.1.0", null, HYPHEN, d20190215));

        // Unicode en dash as separator
        assertThat(parse(asHeading("## [1.1.0] – 2019-02-15")))
                .isEqualTo(Version.of("1.1.0", null, EN_DASH, d20190215));

        // Unicode em dash as separator
        assertThat(parse(asHeading("## [1.1.0] — 2019-02-15")))
                .isEqualTo(Version.of("1.1.0", null, EM_DASH, d20190215));

        assertThat(parse(asHeading("## [1.1.0](https://github.com/olivierlacan/keep-a-changelog/compare/v1.0.0...v1.1.0) - 2019-02-15")))
                .describedAs("Version with direct link")
                .isEqualTo(Version.of("1.1.0", urlOf("https://github.com/olivierlacan/keep-a-changelog/compare/v1.0.0...v1.1.0"), HYPHEN, d20190215));

        assertThat(parse(asHeading("## [Unreleased](https://github.com/olivierlacan/keep-a-changelog/compare/v1.1.0...HEAD)")))
                .describedAs("Unreleased with direct link")
                .isEqualTo(Version.of("Unreleased", urlOf("https://github.com/olivierlacan/keep-a-changelog/compare/v1.1.0...HEAD"), HYPHEN, LocalDate.MAX));

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
                .contains(Version.of("Unreleased", null, HYPHEN, LocalDate.MAX), atIndex(0))
                .contains(Version.of("1.1.0", null, HYPHEN, d20190215), atIndex(1));
    }

    @Test
    public void testToHeading() {
        assertThat(Version.of("Unreleased", null, HYPHEN, LocalDate.MAX).toHeading())
                .extracting(Sample::asText)
                .asString()
                .isEqualTo("## [Unreleased]");

        assertThat(Version.of("1.1.0", null, HYPHEN, d20190215).toHeading())
                .extracting(Sample::asText, STRING)
                .isEqualTo("## [1.1.0] - 2019-02-15");
    }

    @Test
    public void testToString() {
        assertThat(Version.of("Unreleased", null, HYPHEN, LocalDate.MAX))
                .hasToString("Version(ref=Unreleased, separator=\\u002d, date=+999999999-12-31)");

        assertThat(Version.of("1.1.0", null, HYPHEN, d20190215))
                .hasToString("Version(ref=1.1.0, separator=\\u002d, date=2019-02-15)");
    }

    @Test
    public void testParseLocalDate() {
        assertThat(Version.parseLocalDate(null))
                .isEqualTo(LocalDate.now(ZoneId.systemDefault()));

        assertThat(Version.parseLocalDate(""))
                .isEqualTo(LocalDate.now(ZoneId.systemDefault()));

        assertThat(Version.parseLocalDate("2010-01-02"))
                .isEqualTo(LocalDate.of(2010, 1, 2));

        assertThatExceptionOfType(DateTimeParseException.class)
                .isThrownBy(() -> Version.parseLocalDate("2010-01"));
    }

    private final LocalDate d20190215 = LocalDate.parse("2019-02-15");
}
