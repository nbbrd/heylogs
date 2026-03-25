package internal.heylogs.cli;

import lombok.NonNull;
import nbbrd.heylogs.TypeOfChange;
import picocli.CommandLine;

import java.util.Iterator;
import java.util.Locale;
import java.util.stream.Stream;

@lombok.Getter
@lombok.Setter
public class TypeOfChangeOptions {

    @CommandLine.Option(
            names = {"-y", "--type"},
            paramLabel = "<type>",
            description = "Type of change. Valid values: ${COMPLETION-CANDIDATES}.",
            required = true,
            completionCandidates = TypeOfChangeCandidates.class,
            converter = TypeOfChangeConverter.class
    )
    private TypeOfChange typeOfChange;

    public static final class TypeOfChangeConverter implements CommandLine.ITypeConverter<TypeOfChange> {

        @Override
        public TypeOfChange convert(String value) {
            return TypeOfChange.valueOf(value.toUpperCase(Locale.ROOT));
        }
    }

    public static final class TypeOfChangeCandidates implements Iterable<String> {

        @Override
        public @NonNull Iterator<String> iterator() {
            return Stream.of(TypeOfChange.values())
                    .map(type -> type.name().toLowerCase(Locale.ROOT))
                    .iterator();
        }
    }
}
