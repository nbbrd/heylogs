package internal.heylogs.cli;

import lombok.NonNull;
import nbbrd.heylogs.FormatConfig;
import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.FormatLoader;
import picocli.CommandLine;

import java.util.Iterator;

@lombok.Getter
@lombok.Setter
public class FormatOptions {

    @CommandLine.Option(
            names = {"-f", "--format"},
            paramLabel = "<id>",
            description = "Specify the format used to control the appearance of the result. Valid values: ${COMPLETION-CANDIDATES}.",
            completionCandidates = FormatCandidates.class,
            converter = FormatConverter.class
    )
    private FormatConfig format;

    public static final class FormatCandidates implements Iterable<String> {

        @Override
        public @NonNull Iterator<String> iterator() {
            return FormatLoader.load()
                    .stream()
                    .map(Format::getFormatId)
                    .iterator();
        }
    }

    public static final class FormatConverter implements CommandLine.ITypeConverter<FormatConfig> {

        @Override
        public FormatConfig convert(String value) {
            return FormatConfig.parse(value);
        }
    }
}
