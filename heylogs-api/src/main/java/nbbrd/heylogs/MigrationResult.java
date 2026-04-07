package nbbrd.heylogs;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

@lombok.Value
@lombok.Builder
public class MigrationResult {
    @NonNull Path pomFile;
    boolean migrated;
    boolean warning;
    @Nullable String message;
    @Nullable Exception error;
}

