package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.Config;

import java.util.List;

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
}
