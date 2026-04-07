package internal.heylogs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class ConfigMigratorTest {

    @TempDir
    Path tempDir;

    @Test
    public void testMigrateSinglePom() throws IOException {
        // Copy test pom to temp directory
        Path testPom = getTestResource("/migration/enforcer-rules/pom.xml");
        Path targetPom = tempDir.resolve("pom.xml");
        Files.copy(testPom, targetPom, StandardCopyOption.REPLACE_EXISTING);

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
    public void testSkipIfPropertiesExists() throws IOException {
        // Copy test pom to temp directory
        Path testPom = getTestResource("/migration/enforcer-rules/pom.xml");
        Path targetPom = tempDir.resolve("pom.xml");
        Files.copy(testPom, targetPom, StandardCopyOption.REPLACE_EXISTING);

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

    private Path getTestResource(String path) {
        return Paths.get(getClass().getResource(path).getPath().substring(1));
    }
}
