package nbbrd.heylogs;

import org.jspecify.annotations.Nullable;

@lombok.Value
@lombok.Builder
public class Config {

    public static final Config DEFAULT = Config.builder().build();

    @Nullable
    String versionTagPrefix;

    @Nullable
    VersioningConfig versioning;

    @Nullable
    String forgeId;
}
