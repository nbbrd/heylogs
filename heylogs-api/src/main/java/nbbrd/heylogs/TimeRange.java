package nbbrd.heylogs;

import internal.heylogs.SummaryStatistics;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@lombok.Value(staticConstructor = "of")
public class TimeRange {

    public static final TimeRange ALL = TimeRange.of(LocalDate.MIN, LocalDate.MAX);

    public static Collector<LocalDate, ?, Optional<TimeRange>> toTimeRange() {
        Collector<LocalDate, ?, SummaryStatistics<LocalDate>> summarizing = SummaryStatistics.summarizing();
        return Collectors.collectingAndThen(summarizing, TimeRange::of);
    }

    private static Optional<TimeRange> of(SummaryStatistics<LocalDate> stats) {
        return stats.getCount() == 0 ? Optional.empty() : Optional.of(new TimeRange(stats.getMin(), stats.getMax()));
    }

    @lombok.NonNull
    LocalDate from;

    @lombok.NonNull
    LocalDate to;


    public boolean contains(LocalDate date) {
        return from.compareTo(date) <= 0 && date.compareTo(to) <= 0;
    }
}
