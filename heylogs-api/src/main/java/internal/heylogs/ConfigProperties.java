package internal.heylogs;

import lombok.NonNull;
import nbbrd.heylogs.Config;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static java.util.stream.Collectors.joining;

public final class ConfigProperties {

    private ConfigProperties() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static final String TAGGING_KEY = "tagging";
    private static final String VERSIONING_KEY = "versioning";
    private static final String FORGE_KEY = "forge";
    private static final String RULES_KEY = "rules";
    private static final String DOMAINS_KEY = "domains";

    public static @NonNull Config fromProperties(@NonNull Properties properties) {
        return Config
                .builder()
                .taggingOf(properties.getProperty(TAGGING_KEY))
                .versioningOf(properties.getProperty(VERSIONING_KEY))
                .forgeOf(properties.getProperty(FORGE_KEY))
                .rulesOf(splitToList(properties.getProperty(RULES_KEY)))
                .domainsOf(splitToList(properties.getProperty(DOMAINS_KEY)))
                .build();
    }

    public static @NonNull Properties toProperties(@NonNull Config config) {
        Properties properties = new Properties();

        if (config.getTagging() != null) {
            properties.setProperty(TAGGING_KEY, config.getTagging().toString());
        }

        if (config.getVersioning() != null) {
            properties.setProperty(VERSIONING_KEY, config.getVersioning().toString());
        }

        if (config.getForge() != null) {
            properties.setProperty(FORGE_KEY, config.getForge().toString());
        }

        if (!config.getRules().isEmpty()) {
            properties.setProperty(RULES_KEY, config.getRules().stream().map(Object::toString).collect(joining(",")));
        }

        if (!config.getDomains().isEmpty()) {
            properties.setProperty(DOMAINS_KEY, config.getDomains().stream().map(Object::toString).collect(joining(",")));
        }

        return properties;
    }

    private static @Nullable List<String> splitToList(@Nullable String list) {
        return list != null ? Arrays.asList(list.split(",", -1)) : null;
    }
}

