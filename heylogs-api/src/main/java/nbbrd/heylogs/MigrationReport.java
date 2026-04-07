package nbbrd.heylogs;

import internal.heylogs.ConfigMigrator;
import lombok.NonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@lombok.Value
@lombok.Builder
public class MigrationReport {

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

    public static @NonNull MigrationReport migrate(@NonNull Path startDirectory) throws IOException {
        ConfigMigrator.MigrationReport internalReport = ConfigMigrator.migrateConfigs(startDirectory);
        List<MigrationResult> results = new java.util.ArrayList<>();
        for (ConfigMigrator.MigrationResult internalResult : internalReport.getResults()) {
            results.add(MigrationResult.builder()
                    .pomFile(internalResult.getPomFile())
                    .migrated(internalResult.isMigrated())
                    .warning(internalResult.isWarning())
                    .message(internalResult.getMessage())
                    .error(internalResult.getError())
                    .build());
        }
        return MigrationReport.builder().results(results).build();
    }
}


