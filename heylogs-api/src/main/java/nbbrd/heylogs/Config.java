package nbbrd.heylogs;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static java.util.stream.Collectors.toList;

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

    @lombok.Singular
    List<DomainConfig> domains;

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

        public @NonNull Builder rulesOf(@Nullable List<? extends CharSequence> ruleConfigs) {
            return ruleConfigs != null ? rules(ruleConfigs.stream().map(RuleConfig::parse).collect(toList())) : this;
        }

        public @NonNull Builder domainOf(@Nullable CharSequence domainConfig) {
            return domainConfig != null ? domain(DomainConfig.parse(domainConfig)) : this;
        }

        public @NonNull Builder domainsOf(@Nullable List<? extends CharSequence> domainConfigs) {
            return domainConfigs != null ? domains(domainConfigs.stream().map(DomainConfig::parse).collect(toList())) : this;
        }
    }
}
