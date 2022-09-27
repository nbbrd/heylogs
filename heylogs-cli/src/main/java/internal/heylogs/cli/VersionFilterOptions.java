package internal.heylogs.cli;

import nbbrd.heylogs.VersionFilter;
import picocli.CommandLine;

import java.time.LocalDate;
import java.util.regex.Pattern;

@lombok.Getter
@lombok.Setter
public class VersionFilterOptions {

    @CommandLine.Option(
            names = {"--ref"},
            paramLabel = "<ref>",
            description = ""
    )
    private String ref = VersionFilter.DEFAULT.getRef();

    @CommandLine.Option(
            names = {"--unreleased-pattern"},
            paramLabel = "<pattern>",
            description = ""
    )
    private Pattern unreleasedPattern = VersionFilter.DEFAULT.getUnreleasedPattern();

    @CommandLine.Option(
            names = {"--from"},
            paramLabel = "<date>",
            description = "",
            converter = LenientDateConverter.class
    )
    private LocalDate from = VersionFilter.DEFAULT.getFrom();

    @CommandLine.Option(
            names = {"--to"},
            paramLabel = "<date>",
            description = "",
            converter = LenientDateConverter.class
    )
    private LocalDate to = VersionFilter.DEFAULT.getTo();

    @CommandLine.Option(
            names = {"--limit"},
            description = ""
    )
    private int limit = VersionFilter.DEFAULT.getLimit();

    public VersionFilter get() {
        return VersionFilter
                .builder()
                .ref(ref)
                .unreleasedPattern(unreleasedPattern)
                .from(from)
                .to(to)
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
