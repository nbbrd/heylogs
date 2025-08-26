package internal.heylogs.ext.calver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static internal.heylogs.ext.calver.CalVerFormat.parse;
import static internal.heylogs.ext.calver.CalVerSeparator.SEP_DOT;
import static internal.heylogs.ext.calver.CalVerTag.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

class CalVerFormatTest {

    @ParameterizedTest
    @CsvFileSource(resources = "CalVerSamples.csv", useHeadersInDisplayName = true)
    public void testRepresentable(String type, String project, String formats, String examples) {
        for (String format : formats.split("/", -1)) {
            assertThat(parse(format))
                    .describedAs("%s %s", type, project)
                    .hasToString(format);
        }
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testParse() {
        assertThatNullPointerException().isThrownBy(() -> parse(null));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(""))
                .withMessage("CalVer format cannot be empty");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("YY.0M MICRO"))
                .withMessage("CalVer format cannot contain whitespace");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("YY.YY"))
                .withMessage("CalVer format cannot have duplicated tags");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("0M.YY"))
                .withMessage("CalVer format must have ordered tags");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("YY.0M.MIGRO"))
                .withMessage("Unknown tag 'MIGRO'");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("YY.0M?MICRO"))
                .withMessage("Unknown separator '?'");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("YY.0M.WW"))
                .withMessage("CalVer format must not mix week and month/day tags");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("YY.WW.DD"))
                .withMessage("CalVer format must not mix week and month/day tags");

        assertThat(parse("YY.0M.MICRO"))
                .describedAs("Ubuntu")
                .hasToString("YY.0M.MICRO")
                .extracting(CalVerFormat::getTokens, list(CalVerToken.class))
                .containsExactly(TAG_YY, SEP_DOT, TAG_0M, SEP_DOT, TAG_MICRO);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "CalVerSamples.csv", useHeadersInDisplayName = true)
    public void testIsValidVersionWithExamples(String type, String project, String formats, String examples) {
        List<CalVerFormat> calVerFormats = Stream.of(formats.split("/", -1)).map(CalVerFormat::parse).collect(toList());
        for (String example : examples.split(",", -1)) {
            assertThat(calVerFormats.stream().anyMatch(format -> format.isValidVersion(example)))
                    .describedAs("%s %s %s", type, project, example)
                    .isTrue();
        }
    }

    @Test
    public void testIsValidVersion() {
        assertThat(parse("YY.0M.MICRO").isValidVersion("4.10")).isTrue();
        assertThat(parse("YY.0M.MICRO").isValidVersion("4.1")).isFalse();
        assertThat(parse("YY.0M.MICRO").isValidVersion("4.1Z")).isFalse();
        assertThat(parse("YY.0M.MICRO").isValidVersion("2004.10")).isFalse();
        assertThat(parse("YY.0M.MICRO").isValidVersion("4?10")).isFalse();
        assertThat(parse("YY.0M.MICRO").isValidVersion("20.04")).isTrue();
        assertThat(parse("YY.0M.MICRO").isValidVersion("20.04.1")).isTrue();
        assertThat(parse("YY.0M.MICRO").isValidVersion("20.04.1.6")).isTrue();
        assertThat(parse("YY.0M.MICRO").isValidVersion("20.04.1-rc1")).isTrue();
        assertThat(parse("YY.0M.MICRO").isValidVersion("20.04-rc1")).isTrue();
    }
}