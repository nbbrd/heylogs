package nbbrd.heylogs;

import internal.heylogs.ConfigFileLoader;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.stream.Collectors.toList;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class Config {

    public static final Config DEFAULT = Config.builder().build();

    /**
     * Loads configuration from heylogs.properties files in the directory hierarchy.
     * Walks up from the specified directory to find and merge configuration files.
     *
     * @param directory the directory to start searching from (typically project root or working directory)
     * @return the merged configuration, or DEFAULT if no config files found
     */
    @StaticFactoryMethod
    public static @NonNull Config loadFromDirectory(@Nullable Path directory) {
        return ConfigFileLoader.loadConfig(directory);
    }

    /**
     * Resolves the start directory for configuration loading from a nullable input path.
     * Returns the parent directory of the path if it exists, otherwise returns the current directory.
     *
     * @param inputPath the input path to resolve the directory from (can be null)
     * @return the resolved directory path, never null
     */
    public static @NonNull Path resolveStartDir(@Nullable Path inputPath) {
        if (inputPath != null && inputPath.getParent() != null) {
            return inputPath.getParent();
        }
        return Paths.get("").toAbsolutePath();
    }

    /**
     * Resolves the start directory for configuration loading, preferring project basedir over input file.
     * Returns the project basedir if available, otherwise the parent directory of the input file,
     * and falls back to the current directory if neither is available.
     *
     * @param projectBasedir the Maven project basedir (can be null)
     * @param inputFile      the input file to resolve the directory from (can be null)
     * @return the resolved directory path, never null
     */
    public static @NonNull Path resolveStartDir(@Nullable Path projectBasedir, @Nullable Path inputFile) {
        return projectBasedir != null ? projectBasedir : resolveStartDir(inputFile);
    }

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

    /**
     * Merges this configuration with another configuration, with the other configuration taking precedence.
     * Null values in the other configuration are replaced by values from this configuration.
     * Non-empty lists in the other configuration completely replace lists from this configuration.
     *
     * @param other the configuration to merge with (higher precedence)
     * @return a new merged configuration
     */
    public @NonNull Config mergeWith(@NonNull Config other) {
        return Config.builder()
                .tagging(other.getTagging() != null ? other.getTagging() : this.getTagging())
                .versioning(other.getVersioning() != null ? other.getVersioning() : this.getVersioning())
                .forge(other.getForge() != null ? other.getForge() : this.getForge())
                .rules(!other.getRules().isEmpty() ? other.getRules() : this.getRules())
                .domains(!other.getDomains().isEmpty() ? other.getDomains() : this.getDomains())
                .build();
    }

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
