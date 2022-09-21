package internal.heylogs.cli;

import nbbrd.heylogs.VersionFilter;
import picocli.CommandLine;

import java.time.LocalDate;

@lombok.Getter
@lombok.Setter
public class VersionFilterOptions {

    @CommandLine.Option(
            names = {"--ref"},
            paramLabel = "<date>",
            description = ""
    )
    private String ref = "";

    @CommandLine.Option(
            names = {"--from"},
            paramLabel = "<date>",
            description = "",
            converter = CustomConverter.class
    )
    private LocalDate from = LocalDate.MIN;

    @CommandLine.Option(
            names = {"--to"},
            paramLabel = "<date>",
            description = "",
            converter = CustomConverter.class
    )
    private LocalDate to = LocalDate.MAX;

    @CommandLine.Option(
            names = {"--limit"},
            description = ""
    )
    private int limit = Integer.MAX_VALUE;

    public VersionFilter get() {
        return VersionFilter
                .builder()
                .ref(ref)
                .from(from)
                .to(to)
                .limit(limit)
                .build();
    }

    private static final class CustomConverter implements CommandLine.ITypeConverter<LocalDate> {

        @Override
        public LocalDate convert(String value) {
            return VersionFilter.parseLocalDate(value);
        }
    }
}
