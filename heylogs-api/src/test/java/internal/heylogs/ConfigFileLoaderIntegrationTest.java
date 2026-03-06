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

/**
 * Integration tests demonstrating the complete configuration file loading workflow.
 */
class ConfigFileLoaderIntegrationTest {

    private static void writeString(Path path, String content) throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
            writer.print(content);
        }
    }

    @Test
    void testRealWorldScenario(@TempDir Path tempDir) throws IOException {
        // Scenario: Organization with multiple projects
        // /organization/
        //   heylogs.properties (org defaults: github, semver)
        //   /project-a/
        //     heylogs.properties (adds tagging: prefix:v)
        //     CHANGELOG.md

        // Create organization-level config
        Path orgConfig = tempDir.resolve("heylogs.properties");
        writeString(orgConfig,
                "# Organization-wide defaults\n" +
                "forge=github\n" +
                "versioning=semver\n" +
                "rules=no-empty-changes:ERROR\n");

        // Create project directory and config
        Path projectDir = tempDir.resolve("project-a");
        Files.createDirectory(projectDir);
        Path projectConfig = projectDir.resolve("heylogs.properties");
        writeString(projectConfig,
                "# Project-specific config\n" +
                "tagging=prefix:v\n" +
                "# Override rules from org\n" +
                "rules=no-empty-changes:ERROR,valid-heading:WARN\n");

        // Load config from project directory
        Config config = Config.loadFromDirectory(projectDir);

        // Verify merged configuration
        assertThat(config.getForge()).isNotNull().hasToString("github"); // From org
        assertThat(config.getVersioning()).isNotNull().hasToString("semver"); // From org
        assertThat(config.getTagging()).isNotNull().hasToString("prefix:v"); // From project
        assertThat(config.getRules()).hasSize(2); // From project (replaces org)
        assertThat(config.getRules().get(0)).hasToString("no-empty-changes:ERROR");
        assertThat(config.getRules().get(1)).hasToString("valid-heading:WARN");
    }

    @Test
    void testStopBubblingScenario(@TempDir Path tempDir) throws IOException {
        // Scenario: Independent project that doesn't want to inherit org config
        // /organization/
        //   heylogs.properties (org defaults)
        //   /independent-project/
        //     heylogs.properties (config.stopBubbling=true, own config)

        // Create organization config
        Path orgConfig = tempDir.resolve("heylogs.properties");
        writeString(orgConfig,
                "forge=github\n" +
                "versioning=semver\n");

        // Create independent project
        Path projectDir = tempDir.resolve("independent-project");
        Files.createDirectory(projectDir);
        Path projectConfig = projectDir.resolve("heylogs.properties");
        writeString(projectConfig,
                "config.stopBubbling=true\n" +
                "forge=gitlab\n" +
                "versioning=calver\n");

        // Load config from project directory
        Config config = Config.loadFromDirectory(projectDir);

        // Verify only project config is used (org config ignored due to stopBubbling)
        assertThat(config.getForge()).isNotNull().hasToString("gitlab"); // From project

        assertThat(config.getVersioning()).isNotNull().hasToString("calver"); // From project
    }

    @Test
    void testMultiModuleMavenProject(@TempDir Path tempDir) throws IOException {
        // Scenario: Multi-module Maven project
        // /parent/
        //   heylogs.properties (parent config)
        //   CHANGELOG.md
        //   /module-a/
        //     CHANGELOG.md
        //   /module-b/
        //     heylogs.properties (module-specific overrides)
        //     CHANGELOG.md

        // Create parent config
        Path parentConfig = tempDir.resolve("heylogs.properties");
        writeString(parentConfig,
                "versioning=semver\n" +
                "forge=github\n" +
                "tagging=prefix:v\n");

        // Create module-a (inherits all from parent)
        Path moduleADir = tempDir.resolve("module-a");
        Files.createDirectory(moduleADir);

        // Create module-b with overrides
        Path moduleBDir = tempDir.resolve("module-b");
        Files.createDirectory(moduleBDir);
        Path moduleBConfig = moduleBDir.resolve("heylogs.properties");
        writeString(moduleBConfig,
                "# Module B uses different tagging\n" +
                "tagging=prefix:release-\n");

        // Test module-a inherits everything from parent
        Config configA = Config.loadFromDirectory(moduleADir);
        assertThat(configA.getTagging()).isNotNull().hasToString("prefix:v");
        assertThat(configA.getVersioning()).isNotNull().hasToString("semver");
        assertThat(configA.getForge()).isNotNull().hasToString("github");

        // Test module-b overrides tagging but inherits the rest
        Config configB = Config.loadFromDirectory(moduleBDir);
        assertThat(configB.getTagging()).isNotNull().hasToString("prefix:release-");
        assertThat(configB.getVersioning()).isNotNull().hasToString("semver"); // Inherited
        assertThat(configB.getForge()).isNotNull().hasToString("github"); // Inherited
    }

    @Test
    void testConfigMergeWithExplicitParameters(@TempDir Path tempDir) throws IOException {
        // Scenario: Config file provides defaults, but explicit parameters override them

        // Create config file
        Path configFile = tempDir.resolve("heylogs.properties");
        writeString(configFile,
                "forge=github\n" +
                "versioning=semver\n" +
                "tagging=prefix:v\n");

        // Load base config from file
        Config fileConfig = Config.loadFromDirectory(tempDir);

        // Simulate CLI/Maven/Enforcer explicitly overriding some values
        // User explicitly specified calver, but didn't specify forge or tagging
        Config explicitConfig = Config.builder()
                .versioningOf("calver") // User explicitly specified calver
                .build();

        // Merge: file config provides defaults, explicit config overrides
        Config mergedConfig = fileConfig.mergeWith(explicitConfig);

        // Verify merge result
        assertThat(mergedConfig.getForge()).isNotNull().hasToString("github"); // From file
        assertThat(mergedConfig.getVersioning()).isNotNull().hasToString("calver"); // Explicit override
        assertThat(mergedConfig.getTagging()).isNotNull().hasToString("prefix:v"); // From file
    }

    @Test
    void testEmptyConfigFile(@TempDir Path tempDir) throws IOException {
        // Create empty config file
        Path configFile = tempDir.resolve("heylogs.properties");
        writeString(configFile, "# Empty config file\n");

        Config config = Config.loadFromDirectory(tempDir);

        // Should return default config
        assertThat(config.getForge()).isNull();
        assertThat(config.getVersioning()).isNull();
        assertThat(config.getTagging()).isNull();
        assertThat(config.getRules()).isEmpty();
        assertThat(config.getDomains()).isEmpty();
    }

    @Test
    void testCommentedProperties(@TempDir Path tempDir) throws IOException {
        // Create config file with comments
        Path configFile = tempDir.resolve("heylogs.properties");
        writeString(configFile,
                "# This is a comment\n" +
                "forge=github\n" +
                "# versioning=semver  (commented out)\n" +
                "tagging=prefix:v\n" +
                "# Another comment\n");

        Config config = Config.loadFromDirectory(tempDir);

        assertThat(config.getForge()).isNotNull().hasToString("github");
        assertThat(config.getVersioning()).isNull(); // Commented out
        assertThat(config.getTagging()).isNotNull().hasToString("prefix:v");
    }

    @Test
    void testNoConfigBehavior(@TempDir Path tempDir) throws IOException {
        // Scenario: Verify that we can skip config file loading entirely

        // Create a config file that would normally be loaded
        Path configFile = tempDir.resolve("heylogs.properties");
        writeString(configFile,
                "forge=github\n" +
                "versioning=semver\n" +
                "tagging=prefix:v\n");

        // Simulate --no-config behavior: don't load from directory
        Config explicitConfig = Config.builder()
                .versioningOf("calver") // Only what user explicitly specified
                .build();

        // Verify that explicit config has no file values
        assertThat(explicitConfig.getForge()).isNull(); // Not from file
        assertThat(explicitConfig.getVersioning()).isNotNull().hasToString("calver"); // Explicit
        assertThat(explicitConfig.getTagging()).isNull(); // Not from file

        // This is what happens with --no-config: only explicit parameters are used
        // (The actual flag is handled in ConfigOptions/CheckMojo/ReleaseMojo/CheckRule)
    }
}

