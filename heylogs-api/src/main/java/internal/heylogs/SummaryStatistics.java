package internal.heylogs;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.stream.Collector;

@lombok.Getter
@lombok.ToString
@lombok.RequiredArgsConstructor
public final class SummaryStatistics<T> implements Consumer<T> {


    public static <T extends Comparable<? super T>> Collector<T, ?, SummaryStatistics<T>> summarizing() {
        return summarizing(Comparator.naturalOrder());
    }

    public static <T> Collector<T, ?, SummaryStatistics<T>> summarizing(Comparator<? super T> comparator) {
        return Collector.of(
                () -> new SummaryStatistics<>(comparator),
                SummaryStatistics::accept,
                SummaryStatistics::combine,
                Collector.Characteristics.UNORDERED,
                Collector.Characteristics.IDENTITY_FINISH
        );
    }

    private final Comparator<? super T> comparator;
    private long count;
    private T min;
    private T max;


    /**
     * Records a new value into the summary information
     *
     * @param value the input value
     */
    @Override
    public void accept(T value) {
        if (count == 0)
            min = max = value;
        else if (comparator.compare(value, min) < 0)
            min = value;
        else if (comparator.compare(value, max) > 0)
            max = value;

        count++;
    }

    /**
     * Combines the state of another {@code SummaryStatistics} into this one.
     *
     * @param other another {@code SummaryStatistics}
     * @throws NullPointerException if {@code other} is null
     */
    public SummaryStatistics<T> combine(SummaryStatistics<T> other) {
        if (this.count == 0) return other;
        if (other.count == 0) return this;

        this.count += other.count;
        if (comparator.compare(other.min, this.min) < 0)
            this.min = other.min;
        if (comparator.compare(other.max, this.max) > 0)
            this.max = other.max;

        return this;
    }
}
