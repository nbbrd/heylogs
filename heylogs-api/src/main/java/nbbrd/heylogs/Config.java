package nbbrd.heylogs;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

@lombok.Value
@lombok.Builder
public class Config {

    public static final Config DEFAULT = Config.builder().build();

    @NonNull
    @lombok.Builder.Default
    String versionTagPrefix = "";

    @Nullable
    String versioningId;

    @Nullable
    String versioningArg;

    @Nullable
    String forgeId;
}
