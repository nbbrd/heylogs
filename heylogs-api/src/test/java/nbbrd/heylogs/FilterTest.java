package nbbrd.heylogs;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.function.Function;

import static nbbrd.heylogs.Filter.parseLocalDate;
import static nbbrd.heylogs.Version.HYPHEN;
import static org.assertj.core.api.Assertions.*;

public class FilterTest {

    @Test
    public void testRef() {
        assertThat(Filter.builder().build())
                .describedAs("Empty reference")
                .is(containing(unreleased))
                .is(containing(v1_1_0))
                .is(containing(v1_0_0));

        assertThat(Filter.builder().ref("Unreleased").build())
                .describedAs("Full reference")
                .is(containing(unreleased))
                .isNot(containing(v1_1_0))
                .isNot(containing(v1_0_0));

        assertThat(Filter.builder().ref("1.1.0").build())
                .describedAs("Full reference")
                .isNot(containing(unreleased))
                .is(containing(v1_1_0))
                .isNot(containing(v1_0_0));

        assertThat(Filter.builder().ref("rel").build())
                .describedAs("Partial reference")
                .is(containing(unreleased))
                .isNot(containing(v1_1_0))
                .isNot(containing(v1_0_0));

        assertThat(Filter.builder().ref("1.").build())
                .describedAs("Partial reference")
                .isNot(containing(unreleased))
                .is(containing(v1_1_0))
                .is(containing(v1_0_0));

        assertThat(Filter.builder().ref("other").build())
                .describedAs("Unknown reference")
                .isNot(containing(unreleased))
                .isNot(containing(v1_1_0))
                .isNot(containing(v1_0_0));

        assertThat(Filter.builder().ref("other-SNAPSHOT").build())
                .describedAs("Matching unreleased pattern reference")
                .is(containing(unreleased))
                .isNot(containing(v1_1_0))
                .isNot(containing(v1_0_0));
    }

    @Test
    public void testTimeRange() {
        Function<TimeRange, Filter> onTimeRange = o -> Filter.builder().timeRange(o).build();

        assertThat(TimeRange.ALL)
                .extracting(onTimeRange)
                .is(containing(unreleased))
                .is(containing(v1_1_0))
                .is(containing(v1_0_0));

        assertThat(TimeRange.of(v1_0_0.getDate(), v1_1_0.getDate()))
                .extracting(onTimeRange)
                .isNot(containing(unreleased))
                .is(containing(v1_1_0))
                .is(containing(v1_0_0));

        assertThat(TimeRange.of(v1_0_0.getDate(), v1_0_0.getDate()))
                .extracting(onTimeRange)
                .isNot(containing(unreleased))
                .isNot(containing(v1_1_0))
                .is(containing(v1_0_0));

        assertThat(TimeRange.of(LocalDate.MIN, v1_0_0.getDate()))
                .extracting(onTimeRange)
                .isNot(containing(unreleased))
                .isNot(containing(v1_1_0))
                .is(containing(v1_0_0));

        assertThat(TimeRange.of(v1_1_0.getDate(), v1_1_0.getDate()))
                .extracting(onTimeRange)
                .isNot(containing(unreleased))
                .is(containing(v1_1_0))
                .isNot(containing(v1_0_0));

        assertThat(TimeRange.of(v1_1_0.getDate(), LocalDate.MAX))
                .extracting(onTimeRange)
                .is(containing(unreleased))
                .is(containing(v1_1_0))
                .isNot(containing(v1_0_0));
    }

    @Test
    public void testParseLocalDate() {
        assertThatNullPointerException()
                .isThrownBy(() -> parseLocalDate(null));

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> parseLocalDate(""));

        assertThat(parseLocalDate("2010"))
                .isEqualTo("2010-01-01");

        assertThat(parseLocalDate("2010-02"))
                .isEqualTo("2010-02-01");

        assertThat(parseLocalDate("2010-02-03"))
                .isEqualTo("2010-02-03");
    }

    private static Condition<Filter> containing(Version version) {
        return new Condition<>(parent -> parent.contains(version), "Must contain %s", version);
    }

    private final Version unreleased = Version.of("Unreleased", HYPHEN, LocalDate.MAX);
    private final Version v1_1_0 = Version.of("1.1.0", HYPHEN, LocalDate.parse("2019-02-15"));
    private final Version v1_0_0 = Version.of("1.0.0", HYPHEN, LocalDate.parse("2017-06-20"));
}
