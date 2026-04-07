package internal.heylogs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tests.heylogs.api.Files2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.assertj.core.api.Assertions.assertThat;

public class ConfigMigratorTest {

    @Test
    public void testMigrateSinglePom(@TempDir Path tempDir) throws IOException {
        // Copy test pom to temp directory
        Path testPom = Files2.resolveResource(ConfigMigratorTest.class, "/migration/enforcer-rules/pom.xml");
        Files2.copyRecursively(testPom.getParent(), tempDir, REPLACE_EXISTING);

        // Run migration
        ConfigMigrator.MigrationReport report = ConfigMigrator.migrateConfigs(tempDir);

        // Check report
        assertThat(report.getResults()).hasSize(1);
        ConfigMigrator.MigrationResult result = report.getResults().get(0);
        assertThat(result.isMigrated()).isTrue();
        assertThat(result.isWarning()).isFalse();

        // Check properties file was created
        Path propertiesFile = tempDir.resolve("heylogs.properties");
        assertThat(propertiesFile).exists();

        // Check properties content
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(propertiesFile)) {
            props.load(in);
        }
        assertThat(props.getProperty("versioning")).isNotNull().contains("regex");
        assertThat(props.getProperty("tagging")).isEqualTo("prefix:v");
    }

    @Test
    public void testSkipIfPropertiesExists(@TempDir Path tempDir) throws IOException {
        // Copy test pom to temp directory
        Path testPom = Files2.resolveResource(ConfigMigratorTest.class, "/migration/enforcer-rules/pom.xml");
        Files2.copyRecursively(testPom.getParent(), tempDir, REPLACE_EXISTING);

        // Create existing properties file
        Path propertiesFile = tempDir.resolve("heylogs.properties");
        Files.write(propertiesFile, "existing=true".getBytes(UTF_8));

        // Run migration
        ConfigMigrator.MigrationReport report = ConfigMigrator.migrateConfigs(tempDir);

        // Check report
        assertThat(report.getResults()).hasSize(1);
        ConfigMigrator.MigrationResult result = report.getResults().get(0);
        assertThat(result.isMigrated()).isFalse();
        assertThat(result.isWarning()).isTrue();
        assertThat(result.getMessage()).contains("already exists");

        // Check properties file unchanged
        String content = new String(Files.readAllBytes(propertiesFile), UTF_8);
        assertThat(content).isEqualTo("existing=true");
    }
}
