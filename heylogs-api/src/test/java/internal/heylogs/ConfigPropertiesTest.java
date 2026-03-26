package internal.heylogs;

import nbbrd.heylogs.Config;
import nbbrd.heylogs.spi.RuleSeverity;
import nbbrd.io.Properties2;
import nbbrd.io.text.TextFormatter;
import nbbrd.io.text.TextParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigPropertiesTest {

    @Test
    public void testRoundTripDefault() throws IOException {
        Config original = Config.DEFAULT;
        Config restored = roundTrip(original);

        assertThat(restored.getTagging()).isNull();
        assertThat(restored.getVersioning()).isNull();
        assertThat(restored.getForge()).isNull();
        assertThat(restored.getRules()).isEmpty();
        assertThat(restored.getDomains()).isEmpty();
    }

    @Test
    public void testRoundTripFull() throws IOException {
        Config original = Config
                .builder()
                .taggingOf("prefix:v")
                .versioningOf("semver")
                .forgeOf("github")
                .ruleOf("no-empty-changes:ERROR")
                .ruleOf("valid-heading:WARN")
                .domainOf("example.com:github")
                .build();

        Config restored = roundTrip(original);

        assertThat(restored.getTagging()).isNotNull();
        assertThat(restored.getTagging()).hasToString("prefix:v");

        assertThat(restored.getVersioning()).isNotNull();
        assertThat(restored.getVersioning()).hasToString("semver");

        assertThat(restored.getForge()).isNotNull();
        assertThat(restored.getForge()).hasToString("github");

        assertThat(restored.getRules()).hasSize(2);
        assertThat(restored.getRules().get(0)).hasToString("no-empty-changes:ERROR");
        assertThat(restored.getRules().get(1)).hasToString("valid-heading:WARN");

        assertThat(restored.getDomains()).hasSize(1);
        assertThat(restored.getDomains().get(0)).hasToString("example.com:github");
    }

    @Test
    public void testFromProperties() {
        Properties properties = new Properties();
        properties.setProperty("tagging", "prefix:v");
        properties.setProperty("versioning", "regex:.*");
        properties.setProperty("forge", "github");
        properties.setProperty("rules", "no-empty-changes:ERROR");
        properties.setProperty("domains", "example.com:github");

        Config config = ConfigProperties.fromProperties(properties);

        assertThat(config.getTagging()).isNotNull();
        assertThat(config.getTagging().getId()).isEqualTo("prefix");
        assertThat(config.getTagging().getArg()).isEqualTo("v");

        assertThat(config.getVersioning()).isNotNull();
        assertThat(config.getVersioning().getId()).isEqualTo("regex");
        assertThat(config.getVersioning().getArg()).isEqualTo(".*");

        assertThat(config.getForge()).isNotNull();
        assertThat(config.getForge().getId()).isEqualTo("github");

        assertThat(config.getRules()).hasSize(1);
        assertThat(config.getRules().get(0).getId()).isEqualTo("no-empty-changes");
        assertThat(config.getRules().get(0).getSeverity()).isEqualTo(RuleSeverity.ERROR);

        assertThat(config.getDomains()).hasSize(1);
        assertThat(config.getDomains().get(0).getDomain()).isEqualTo("example.com");
        assertThat(config.getDomains().get(0).getForgeId()).isEqualTo("github");
    }

    @Test
    public void testToProperties() {
        Config config = Config
                .builder()
                .taggingOf("prefix:v")
                .versioningOf("semver")
                .forgeOf("github")
                .ruleOf("no-empty-changes:ERROR")
                .domainOf("example.com:github")
                .build();

        Properties properties = ConfigProperties.toProperties(config);

        assertThat(properties.getProperty("tagging")).isEqualTo("prefix:v");
        assertThat(properties.getProperty("versioning")).isEqualTo("semver");
        assertThat(properties.getProperty("forge")).isEqualTo("github");
        assertThat(properties.getProperty("rules")).isEqualTo("no-empty-changes:ERROR");
        assertThat(properties.getProperty("domains")).isEqualTo("example.com:github");
    }

    @Test
    public void testPartialConfig() {
        Properties properties = new Properties();
        properties.setProperty("versioning", "semver");

        Config config = ConfigProperties.fromProperties(properties);

        assertThat(config.getTagging()).isNull();
        assertThat(config.getVersioning()).isNotNull();
        assertThat(config.getVersioning().getId()).isEqualTo("semver");
        assertThat(config.getForge()).isNull();
        assertThat(config.getRules()).isEmpty();
        assertThat(config.getDomains()).isEmpty();
    }

    @Test
    public void testReadWrite() throws IOException {
        Config original = Config
                .builder()
                .versioningOf("semver")
                .forgeOf("github")
                .ruleOf("no-empty-changes")
                .build();

        String formatted = formatter.formatToString(original);
        Config restored = parser.parseChars(formatted);

        assertThat(restored.getVersioning()).isEqualTo(original.getVersioning());
        assertThat(restored.getForge()).isEqualTo(original.getForge());
        assertThat(restored.getRules()).hasSize(1);
        assertThat(restored.getRules().get(0).getId()).isEqualTo("no-empty-changes");
    }

    private Config roundTrip(Config config) throws IOException {
        return parser.parseChars(formatter.formatToString(config));
    }

    private final TextParser<Config> parser = TextParser
            .onParsingReader(Properties2::loadFromReader)
            .andThen(ConfigProperties::fromProperties);

    private final TextFormatter<Config> formatter = TextFormatter
            .onFormattingWriter(Properties2::storeToWriter)
            .compose(ConfigProperties::toProperties);
}

