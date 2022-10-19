package internal.heylogs.cli;

import nbbrd.heylogs.FailureFormatter;
import nbbrd.heylogs.FailureFormatterLoader;
import picocli.CommandLine;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

@lombok.Getter
@lombok.Setter
public class FailureFormatOptions {

    @CommandLine.Option(
            names = {"-f", "--formatter"},
            paramLabel = "<name>",
            defaultValue = "stylish",
            description = "Specify the formatter used to control the appearance of the result. Valid values: ${COMPLETION-CANDIDATES}.",
            completionCandidates = FailureFormatters.class,
            converter = FailureFormatters.class
    )
    private FailureFormatter formatter;

    public static final class FailureFormatters implements Iterable<String>, CommandLine.ITypeConverter<FailureFormatter> {

        private final List<FailureFormatter> formatters = FailureFormatterLoader.load();

        @Override
        public Iterator<String> iterator() {
            return formatters
                    .stream()
                    .map(FailureFormatter::getName)
                    .iterator();
        }

        @Override
        public FailureFormatter convert(String value) throws Exception {
            return formatters
                    .stream()
                    .filter(formatter -> formatter.getName().equals(value))
                    .findFirst()
                    .orElseThrow(NoSuchElementException::new);
        }
    }
}
