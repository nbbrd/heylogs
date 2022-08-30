package internal.heylogs.cli;

import com.vladsch.flexmark.ast.Heading;
import nbbrd.heylogs.Version;
import picocli.CommandLine;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;

@lombok.Getter
@lombok.Setter
public class VersionFilter {

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

    public boolean contains(Heading heading) {
        return contains(Version.parse(heading));
    }

    public boolean contains(Version version) {
        return version.getRef().contains(ref)
                && from.compareTo(version.getDate()) <= 0
                && (to.isAfter(version.getDate()) || (to.equals(LocalDate.MAX) && version.isUnreleased()));
    }

    private static final class CustomConverter implements CommandLine.ITypeConverter<LocalDate> {

        @Override
        public LocalDate convert(String value) throws Exception {
            try {
                return Year.parse(value).atDay(1);
            } catch (Exception ex1) {
                try {
                    return YearMonth.parse(value).atDay(1);
                } catch (Exception ex2) {
                    return LocalDate.parse(value);
                }
            }
        }
    }
}
