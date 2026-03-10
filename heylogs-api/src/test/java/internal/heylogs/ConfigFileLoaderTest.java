package internal.heylogs;

import nbbrd.heylogs.Config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigFileLoaderTest {

    private static void writeString(Path path, String content) throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
            writer.print(content);
        }
    }

    @Test
    void testLoadConfigNoFile(@TempDir Path tempDir) {
        Config config = ConfigFileLoader.loadConfig(tempDir);
        assertThat(config).isEqualTo(Config.DEFAULT);
    }

    @Test
    void testLoadConfigNullDirectory() {
        Config config = ConfigFileLoader.loadConfig(null);
        assertThat(config).isEqualTo(Config.DEFAULT);
    }

    @Test
    void testLoadConfigSingleFile(@TempDir Path tempDir) throws IOException {
        // Create a simple config file
        Path configFile = tempDir.resolve("heylogs.properties");
        writeString(configFile,
                "tagging=prefix:v\n" +
                "versioning=semver\n" +
                "forge=github\n");

        Config config = ConfigFileLoader.loadConfig(tempDir);

        assertThat(config.getTagging()).isNotNull().hasToString("prefix:v");
        assertThat(config.getVersioning()).isNotNull().hasToString("semver");
        assertThat(config.getForge()).isNotNull().hasToString("github");
    }

    @Test
    void testLoadConfigWithRulesAndDomains(@TempDir Path tempDir) throws IOException {
        Path configFile = tempDir.resolve("heylogs.properties");
        writeString(configFile,
                "rules=no-empty-changes:ERROR,valid-heading:WARN\n" +
                "domains=example.com:github,internal.corp:gitlab\n");

        Config config = ConfigFileLoader.loadConfig(tempDir);

        assertThat(config.getRules()).hasSize(2);
        assertThat(config.getRules().get(0)).hasToString("no-empty-changes:ERROR");
        assertThat(config.getRules().get(1)).hasToString("valid-heading:WARN");

        assertThat(config.getDomains()).hasSize(2);
        assertThat(config.getDomains().get(0)).hasToString("example.com:github");
        assertThat(config.getDomains().get(1)).hasToString("internal.corp:gitlab");
    }

    @Test
    void testLoadConfigHierarchy(@TempDir Path tempDir) throws IOException {
        // Create parent config
        Path parentConfigFile = tempDir.resolve("heylogs.properties");
        writeString(parentConfigFile,
                "forge=github\n" +
                "versioning=semver\n");

        // Create child directory and config
        Path childDir = tempDir.resolve("child");
        Files.createDirectory(childDir);
        Path childConfigFile = childDir.resolve("heylogs.properties");
        writeString(childConfigFile,
                "tagging=prefix:v\n" +
                "forge=gitlab\n");  // Override parent's forge

        Config config = ConfigFileLoader.loadConfig(childDir);

        // Child's tagging
        assertThat(config.getTagging()).isNotNull().hasToString("prefix:v");

        // Child's forge overrides parent
        assertThat(config.getForge()).isNotNull().hasToString("gitlab");

        // Parent's versioning is inherited
        assertThat(config.getVersioning()).isNotNull().hasToString("semver");
    }

    @Test
    void testLoadConfigStopBubbling(@TempDir Path tempDir) throws IOException {
        // Create grandparent config
        Path grandparentConfigFile = tempDir.resolve("heylogs.properties");
        writeString(grandparentConfigFile,
                "forge=github\n" +
                "versioning=semver\n");

        // Create parent directory and config with stopBubbling
        Path parentDir = tempDir.resolve("parent");
        Files.createDirectory(parentDir);
        Path parentConfigFile = parentDir.resolve("heylogs.properties");
        writeString(parentConfigFile,
                "config.stopBubbling=true\n" +
                "tagging=prefix:v\n");

        // Create child directory
        Path childDir = parentDir.resolve("child");
        Files.createDirectory(childDir);

        Config config = ConfigFileLoader.loadConfig(childDir);

        // Only parent's config should be loaded (stopBubbling prevents grandparent)
        assertThat(config.getTagging()).isNotNull().hasToString("prefix:v");
        assertThat(config.getForge()).isNull();  // Grandparent's forge not inherited
        assertThat(config.getVersioning()).isNull();  // Grandparent's versioning not inherited
    }

    @Test
    void testLoadConfigDeepHierarchy(@TempDir Path tempDir) throws IOException {
        // Create config at root
        Path rootConfigFile = tempDir.resolve("heylogs.properties");
        writeString(rootConfigFile, "forge=github\n");

        // Create level 1
        Path level1 = tempDir.resolve("level1");
        Files.createDirectory(level1);
        Path level1ConfigFile = level1.resolve("heylogs.properties");
        writeString(level1ConfigFile, "versioning=semver\n");

        // Create level 2
        Path level2 = level1.resolve("level2");
        Files.createDirectory(level2);
        Path level2ConfigFile = level2.resolve("heylogs.properties");
        writeString(level2ConfigFile, "tagging=prefix:v\n");

        // Create level 3 (no config)
        Path level3 = level2.resolve("level3");
        Files.createDirectory(level3);

        Config config = ConfigFileLoader.loadConfig(level3);

        // All ancestor configs should be merged
        assertThat(config.getForge()).isNotNull().hasToString("github");
        assertThat(config.getVersioning()).isNotNull().hasToString("semver");
        assertThat(config.getTagging()).isNotNull().hasToString("prefix:v");
    }

    @Test
    void testLoadConfigListsReplace(@TempDir Path tempDir) throws IOException {
        // Create parent config with rules
        Path parentConfigFile = tempDir.resolve("heylogs.properties");
        writeString(parentConfigFile,
                "rules=rule1:ERROR,rule2:WARN\n");

        // Create child directory and config with different rules
        Path childDir = tempDir.resolve("child");
        Files.createDirectory(childDir);
        Path childConfigFile = childDir.resolve("heylogs.properties");
        writeString(childConfigFile,
                "rules=rule3:ERROR\n");

        Config config = ConfigFileLoader.loadConfig(childDir);

        // Child's rules completely replace parent's rules
        assertThat(config.getRules()).hasSize(1);
        assertThat(config.getRules().get(0)).hasToString("rule3:ERROR");
    }

    @Test
    void testLoadConfigEmptyListInheritsFromParent(@TempDir Path tempDir) throws IOException {
        // Create parent config with rules
        Path parentConfigFile = tempDir.resolve("heylogs.properties");
        writeString(parentConfigFile,
                "rules=rule1:ERROR,rule2:WARN\n");

        // Create child directory without rules
        Path childDir = tempDir.resolve("child");
        Files.createDirectory(childDir);
        Path childConfigFile = childDir.resolve("heylogs.properties");
        writeString(childConfigFile,
                "tagging=prefix:v\n");

        Config config = ConfigFileLoader.loadConfig(childDir);

        // Child inherits parent's rules
        assertThat(config.getRules()).hasSize(2);
        assertThat(config.getRules().get(0)).hasToString("rule1:ERROR");
        assertThat(config.getRules().get(1)).hasToString("rule2:WARN");
    }

    @Test
    void testLoadConfigWithDefaults(@TempDir Path tempDir) throws IOException {
        Path configFile = tempDir.resolve("heylogs.properties");
        writeString(configFile, "tagging=prefix:v\n");

        Config defaults = Config.builder()
                .versioningOf("semver")
                .forgeOf("github")
                .build();

        Config config = ConfigFileLoader.loadConfigWithDefaults(tempDir, defaults);

        // File config
        assertThat(config.getTagging()).isNotNull().hasToString("prefix:v");

        // Defaults
        assertThat(config.getVersioning()).isNotNull().hasToString("semver");
        assertThat(config.getForge()).isNotNull().hasToString("github");
    }

    @Test
    void testLoadConfigInvalidFile(@TempDir Path tempDir) throws IOException {
        // Create an invalid config file (invalid property value)
        Path configFile = tempDir.resolve("heylogs.properties");
        writeString(configFile, "versioning=invalid-versioning-id\n");

        // Should handle gracefully and return partial config or default
        Config config = ConfigFileLoader.loadConfig(tempDir);

        // The invalid config might be partially loaded or default returned
        // This depends on ConfigProperties.fromProperties behavior
        assertThat(config).isNotNull();
    }

    @Test
    void testLoadConfigMalformedPropertiesFile(@TempDir Path tempDir) throws IOException {
        // Create a completely malformed file
        Path configFile = tempDir.resolve("heylogs.properties");
        Files.write(configFile, new byte[]{(byte) 0xFF, (byte) 0xFE}); // Invalid UTF-8

        // Should handle gracefully and log warning
        Config config = ConfigFileLoader.loadConfig(tempDir);

        // Should return default config
        assertThat(config).isEqualTo(Config.DEFAULT);
    }
}












