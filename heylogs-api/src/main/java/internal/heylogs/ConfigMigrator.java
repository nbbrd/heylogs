package internal.heylogs;

import lombok.NonNull;
import nbbrd.heylogs.Config;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class ConfigMigrator {

    private ConfigMigrator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @lombok.Value
    @lombok.Builder
    public static class MigrationReport {
        @lombok.Singular
        List<MigrationResult> results;

        public boolean hasErrors() {
            return results.stream().anyMatch(r -> r.getError() != null);
        }

        public boolean hasWarnings() {
            return results.stream().anyMatch(MigrationResult::isWarning);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (MigrationResult result : results) {
                sb.append(result.getPomFile()).append(": ");
                if (result.isMigrated()) {
                    sb.append("MIGRATED");
                } else if (result.isWarning()) {
                    sb.append("WARNING");
                } else if (result.getError() != null) {
                    sb.append("ERROR");
                } else {
                    sb.append("SKIPPED");
                }
                if (result.getMessage() != null) {
                    sb.append(" - ").append(result.getMessage());
                }
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    @lombok.Value
    @lombok.Builder
    public static class MigrationResult {
        @NonNull
        Path pomFile;
        boolean migrated;
        boolean warning;
        @Nullable String message;
        @Nullable Exception error;
    }

    public static @NonNull MigrationReport migrateConfigs(@NonNull Path startDirectory) throws IOException {
        List<MigrationResult> results = new ArrayList<>();
        migrateConfigsRecursive(startDirectory, results);
        return MigrationReport.builder().results(results).build();
    }

    private static void migrateConfigsRecursive(@NonNull Path directory, @NonNull List<MigrationResult> results) throws IOException {
        Path pomFile = directory.resolve("pom.xml");

        if (!Files.exists(pomFile)) {
            return;
        }

        // Process current pom.xml
        MigrationResult result = migrateSinglePom(pomFile);
        results.add(result);

        // Find and process modules
        try {
            List<String> modules = PomParser.extractModules(pomFile);
            for (String module : modules) {
                Path moduleDir = directory.resolve(module);
                if (Files.isDirectory(moduleDir)) {
                    migrateConfigsRecursive(moduleDir, results);
                }
            }
        } catch (Exception e) {
            results.add(MigrationResult.builder()
                    .pomFile(pomFile)
                    .migrated(false)
                    .warning(false)
                    .message("Failed to read modules")
                    .error(e)
                    .build());
        }
    }

    private static @NonNull MigrationResult migrateSinglePom(@NonNull Path pomFile) {
        Path propertiesFile = pomFile.getParent().resolve("heylogs.properties");

        // Check if properties file already exists
        if (Files.exists(propertiesFile)) {
            return MigrationResult.builder()
                    .pomFile(pomFile)
                    .migrated(false)
                    .warning(true)
                    .message("heylogs.properties already exists, skipping migration")
                    .build();
        }

        try {
            // Extract configuration from pom.xml
            Config config = PomParser.extractHeylogsConfig(pomFile);

            // Check if any configuration exists
            if (isEmptyConfig(config)) {
                return MigrationResult.builder()
                        .pomFile(pomFile)
                        .migrated(false)
                        .warning(false)
                        .message("No heylogs configuration found")
                        .build();
            }

            // Write heylogs.properties
            write(propertiesFile, config);

            // Remove configuration from pom.xml
            PomParser.removeHeylogsConfig(pomFile);

            return MigrationResult.builder()
                    .pomFile(pomFile)
                    .migrated(true)
                    .warning(false)
                    .message("Successfully migrated configuration")
                    .build();

        } catch (Exception e) {
            return MigrationResult.builder()
                    .pomFile(pomFile)
                    .migrated(false)
                    .warning(false)
                    .message("Migration failed: " + e.getMessage())
                    .error(e)
                    .build();
        }
    }

    private static boolean isEmptyConfig(@NonNull Config config) {
        return config.getVersioning() == null
                && config.getTagging() == null
                && config.getForge() == null
                && config.getRules().isEmpty()
                && config.getDomains().isEmpty();
    }

    public static void write(@NonNull Path path, @NonNull Config config) throws IOException {
        Properties properties = ConfigProperties.toProperties(config);
        try (OutputStream out = Files.newOutputStream(path);
             Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            properties.store(writer, null);
        }
    }
}
