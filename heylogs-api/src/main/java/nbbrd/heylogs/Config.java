package nbbrd.heylogs;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class Config {

    public static final Config DEFAULT = Config.builder().build();

    @Nullable
    TaggingConfig tagging;

    @Nullable
    VersioningConfig versioning;

    @Nullable
    ForgeConfig forge;

    @lombok.Singular
    List<RuleConfig> rules;

    public static final class Builder {

        public @NonNull Builder forgeOf(@Nullable CharSequence forgeConfig) {
            return forge(forgeConfig != null ? ForgeConfig.parse(forgeConfig) : null);
        }

        public @NonNull Builder taggingOf(@Nullable CharSequence taggingConfig) {
            return tagging(taggingConfig != null ? TaggingConfig.parse(taggingConfig) : null);
        }

        public @NonNull Builder versioningOf(@Nullable CharSequence versioningConfig) {
            return versioning(versioningConfig != null ? VersioningConfig.parse(versioningConfig) : null);
        }

        public @NonNull Builder ruleOf(@Nullable CharSequence ruleConfig) {
            return ruleConfig != null ? rule(RuleConfig.parse(ruleConfig)) : this;
        }
    }
}
