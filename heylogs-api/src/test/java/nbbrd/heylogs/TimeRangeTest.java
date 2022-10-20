package nbbrd.heylogs;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class TimeRangeTest {

    @Test
    public void testContains() {
        assertThatNullPointerException()
                .isThrownBy(() -> TimeRange.ALL.contains(null));

        assertThat(TimeRange.ALL)
                .is(containing(LocalDate.MIN))
                .is(containing(LocalDate.MAX))
                .is(containing(first))
                .is(containing(last));

        assertThat(TimeRange.of(first, last))
                .isNot(containing(LocalDate.MIN))
                .isNot(containing(LocalDate.MAX))
                .is(containing(first))
                .is(containing(last));
    }

    @Test
    public void testToTimeRange() {
        assertThat(Stream.<LocalDate>empty().collect(TimeRange.toTimeRange()))
                .isEmpty();

        assertThat(Stream.of(first).collect(TimeRange.toTimeRange()))
                .hasValue(TimeRange.of(first, first));

        assertThat(Stream.of(first, last).collect(TimeRange.toTimeRange()))
                .hasValue(TimeRange.of(first, last));

        assertThat(Stream.of(last, first).collect(TimeRange.toTimeRange()))
                .hasValue(TimeRange.of(first, last));
    }

    private final LocalDate first = LocalDate.of(2010, 1, 1);
    private final LocalDate last = LocalDate.of(2011, 1, 1);

    private static Condition<TimeRange> containing(LocalDate date) {
        return new Condition<>(parent -> parent.contains(date), "Must contain %s", date);
    }
}