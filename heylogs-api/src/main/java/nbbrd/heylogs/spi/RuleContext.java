package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.RuleConfig;
import nbbrd.heylogs.VersioningConfig;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

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

    public @Nullable Predicate<CharSequence> findVersioningPredicateOrNull() {
        VersioningConfig versioningConfig = config.getVersioning();
        return versioningConfig != null
                ? versionings.stream()
                .filter(onVersioningId(versioningConfig.getId()))
                .findFirst()
                .map(v -> v.getVersioningPredicateOrNull(versioningConfig.getArg()))
                .orElse(null)
                : null;
    }

    public @Nullable RuleSeverity findRuleSeverityOrNull(@NonNull String ruleId) {
        return config.getRules()
                .stream()
                .filter(ruleConfig -> ruleConfig.getId().equals(ruleId))
                .findFirst()
                .map(RuleConfig::getSeverity)
                .orElse(null);
    }
}
