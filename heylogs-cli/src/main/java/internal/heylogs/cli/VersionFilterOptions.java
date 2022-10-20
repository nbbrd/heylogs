package internal.heylogs.cli;

import nbbrd.heylogs.TimeRange;
import nbbrd.heylogs.VersionFilter;
import picocli.CommandLine;

import java.time.LocalDate;
import java.util.regex.Pattern;

@lombok.Getter
@lombok.Setter
public class VersionFilterOptions {

    @CommandLine.Option(
            names = {"-r", "--ref"},
            paramLabel = "<ref>",
            description = "Filter versions by name."
    )
    private String ref = VersionFilter.DEFAULT.getRef();

    @CommandLine.Option(
            names = {"-u", "--unreleased"},
            paramLabel = "<pattern>",
            description = "Assume that versions that match this pattern are unreleased."
    )
    private Pattern unreleasedPattern = VersionFilter.DEFAULT.getUnreleasedPattern();

    @CommandLine.Option(
            names = {"-f", "--from"},
            paramLabel = "<date>",
            description = "Filter versions by min date (included).",
            converter = LenientDateConverter.class
    )
    private LocalDate from = VersionFilter.DEFAULT.getTimeRange().getFrom();

    @CommandLine.Option(
            names = {"-t", "--to"},
            paramLabel = "<date>",
            description = "Filter versions by max date (included).",
            converter = LenientDateConverter.class
    )
    private LocalDate to = VersionFilter.DEFAULT.getTimeRange().getTo();

    @CommandLine.Option(
            names = {"-l", "--limit"},
            description = "Limit the number of versions."
    )
    private int limit = VersionFilter.DEFAULT.getLimit();

    public VersionFilter get() {
        return VersionFilter
                .builder()
                .ref(ref)
                .unreleasedPattern(unreleasedPattern)
                .timeRange(TimeRange.of(from, to))
                .limit(limit)
                .build();
    }

    private static final class LenientDateConverter implements CommandLine.ITypeConverter<LocalDate> {

        @Override
        public LocalDate convert(String value) {
            return VersionFilter.parseLocalDate(value);
        }
    }
}
