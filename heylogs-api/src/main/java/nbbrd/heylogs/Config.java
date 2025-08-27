package nbbrd.heylogs;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

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

    @lombok.Singular
    List<RuleConfig> rules;

    public static final class Builder {

        public @NonNull Builder versioningOf(@Nullable CharSequence versioningConfig) {
            return versioningConfig != null ? versioning(VersioningConfig.parse(versioningConfig)) : this;
        }

        public @NonNull Builder ruleOf(@Nullable CharSequence ruleConfig) {
            return ruleConfig != null ? rule(RuleConfig.parse(ruleConfig)) : this;
        }
    }
}
