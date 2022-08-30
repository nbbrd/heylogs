package internal.heylogs.cli;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SpecialProperties {

    public static final String DEBUG_OPTION = "--debug";
    public static final String BATCH_OPTION = "--batch";
    public static final String NO_CONFIG_OPTION = "--no-config";

    @StaticFactoryMethod
    public static @NonNull SpecialProperties parse(@NonNull String[] args) {
        List<String> tmp = Arrays.asList(args);
        return new SpecialProperties(
                tmp.contains(DEBUG_OPTION),
                tmp.contains(BATCH_OPTION),
                tmp.contains(NO_CONFIG_OPTION)
        );
    }

    boolean debugRequired;
    boolean batchRequired;
    boolean noConfig;

    public void apply(Properties properties) {
        if (debugRequired) {
        }
        if (batchRequired) {
            disableAnsi(properties);
        }
    }

    public static void disableAnsi(Properties properties) {
        properties.setProperty("picocli.ansi", "false");
        properties.setProperty("org.fusesource.jansi.Ansi.disable", "true");
    }
}
