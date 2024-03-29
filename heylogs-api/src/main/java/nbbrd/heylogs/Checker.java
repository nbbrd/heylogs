package nbbrd.heylogs;

import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.FormatLoader;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleLoader;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class Checker {

    @StaticFactoryMethod
    public static @NonNull Checker ofServiceLoader() {
        return Checker
                .builder()
                .rules(RuleLoader.load())
                .formats(FormatLoader.load())
                .build();
    }

    @NonNull
    @lombok.Singular
    List<Rule> rules;

    @NonNull
    @lombok.Singular
    List<Format> formats;

    @NonNull
    @lombok.Builder.Default
    String formatId = FIRST_FORMAT_AVAILABLE;

    public @NonNull List<Failure> validate(@NonNull Document doc) {
        return Stream.concat(Stream.of(doc), Nodes.of(Node.class).descendants(doc))
                .flatMap(node -> rules.stream().map(rule -> rule.validate(node)).filter(Objects::nonNull))
                .collect(Collectors.toList());
    }

    public void formatFailures(@NonNull Appendable appendable, @NonNull String source, @NonNull List<Failure> failures) throws IOException {
        getFormatById().formatFailures(appendable, source, failures);
    }

    private Format getFormatById() throws IOException {
        return formats.stream()
                .filter(format -> formatId.equals(FIRST_FORMAT_AVAILABLE) || format.getId().equals(formatId))
                .findFirst()
                .orElseThrow(() -> new IOException("Cannot find format '" + formatId + "'"));
    }

    private static final String FIRST_FORMAT_AVAILABLE = "";
}
