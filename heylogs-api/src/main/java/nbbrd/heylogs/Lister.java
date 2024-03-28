package nbbrd.heylogs;

import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.FormatLoader;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleLoader;

import java.io.IOException;
import java.util.List;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class Lister {

    @StaticFactoryMethod
    public static @NonNull Lister ofServiceLoader() {
        return Lister
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

    public void format(@NonNull Appendable appendable, @NonNull List<Resource> resources) throws IOException {
        getFormatById().formatResources(appendable, resources);
    }

    private Format getFormatById() throws IOException {
        return formats.stream()
                .filter(format -> formatId.equals(FIRST_FORMAT_AVAILABLE) || format.getId().equals(formatId))
                .findFirst()
                .orElseThrow(() -> new IOException("Cannot find format '" + formatId + "'"));
    }

    private static final String FIRST_FORMAT_AVAILABLE = "";
}
