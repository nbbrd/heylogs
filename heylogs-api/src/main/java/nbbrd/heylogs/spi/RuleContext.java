package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.Config;

import java.util.List;
import java.util.Optional;

import static nbbrd.heylogs.spi.VersioningSupport.onVersioningId;

@lombok.Value
@lombok.Builder
public class RuleContext {

    public static final RuleContext DEFAULT = RuleContext.builder().build();

    @NonNull
    @lombok.Builder.Default
    Config config = Config.DEFAULT;

    @lombok.Singular
    List<Forge> forges;

    @lombok.Singular
    List<Versioning> versionings;

    public @NonNull Optional<Versioning> findVersioning() {
        return config.getVersioningId() != null
                ? versionings.stream().filter(onVersioningId(config.getVersioningId())).findFirst()
                : Optional.empty();
    }
}
