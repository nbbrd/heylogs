package nbbrd.heylogs;

import lombok.NonNull;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.regex.Pattern;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class Filter {

    public static final Filter DEFAULT = Filter.builder().build();

    @lombok.NonNull
    @lombok.Builder.Default
    String ref = "";

    @lombok.NonNull
    @lombok.Builder.Default
    Pattern unreleasedPattern = Pattern.compile("^.*-SNAPSHOT$");

    @lombok.NonNull
    @lombok.Builder.Default
    TimeRange timeRange = TimeRange.ALL;

    @lombok.Builder.Default
    int limit = Integer.MAX_VALUE;

    @lombok.Builder.Default
    boolean ignoreContent = false;

    private boolean isUnreleasedPattern() {
        return unreleasedPattern.asPredicate().test(ref);
    }

    private boolean containsRef(@NonNull Version version) {
        return (isUnreleasedPattern() && version.isUnreleased()) || version.getRef().contains(ref);
    }

    public boolean contains(@NonNull Version version) {
        return containsRef(version) && timeRange.contains(version.getDate());
    }

    public static @NonNull LocalDate parseLocalDate(@NonNull CharSequence input) {
        try {
            return Year.parse(input).atDay(1);
        } catch (Exception ex1) {
            try {
                return YearMonth.parse(input).atDay(1);
            } catch (Exception ex2) {
                return LocalDate.parse(input);
            }
        }
    }
}
