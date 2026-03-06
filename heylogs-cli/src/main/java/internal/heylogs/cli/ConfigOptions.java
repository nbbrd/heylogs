package internal.heylogs.cli;

import nbbrd.heylogs.*;
import org.jspecify.annotations.Nullable;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;

import static java.util.Collections.emptyList;

@lombok.Getter
public class ConfigOptions {

    @CommandLine.Option(
            names = {"-t", "--tagging"},
            paramLabel = "<id:arg>",
            description = "Specify the tagging used to control the tag references. Valid values: ${COMPLETION-CANDIDATES}.",
            completionCandidates = TaggingCandidates.class,
            converter = TaggingConverter.class
    )
    private TaggingConfig tagging;

    @CommandLine.Option(
            names = {"-v", "--versioning"},
            paramLabel = "<id:arg>",
            description = "Specify the versioning used to control the version references. Valid values: ${COMPLETION-CANDIDATES}.",
            completionCandidates = VersioningCandidates.class,
            converter = VersioningConverter.class
    )
    private VersioningConfig versioning;

    @CommandLine.Option(
            names = {"-g", "--forge"},
            paramLabel = "<id>",
            description = "Specify the forge used to host the versions. Valid values: ${COMPLETION-CANDIDATES}.",
            completionCandidates = ForgeCandidates.class,
            converter = ForgeConverter.class
    )
    private ForgeConfig forge;

    @CommandLine.Option(
            names = {"-u", "--rule"},
            paramLabel = "<id:severity>",
            description = "Specify the rule severity used to check changelogs. Valid values: ${COMPLETION-CANDIDATES}.",
            completionCandidates = RuleCandidates.class,
            converter = RuleConverter.class
    )
    private List<RuleConfig> rules;

    @CommandLine.Option(
            names = {"-m", "--domain"},
            paramLabel = "<domain:forge>",
            description = "Specify the forge used for a specific domain.",
            converter = DomainConverter.class
    )
    private List<DomainConfig> domains;

    @CommandLine.Option(
            names = {"--no-config"},
            description = "Ignore heylogs.properties configuration files."
    )
    private boolean noConfig;

    public Config getConfig() {
        return Config
                .builder()
                .tagging(tagging)
                .versioning(versioning)
                .forge(forge)
                .rules(rules != null ? rules : emptyList())
                .domains(domains != null ? domains : emptyList())
                .build();
    }

    /**
     * Gets configuration by first loading from heylogs.properties files in the directory hierarchy,
     * then merging with command-line options (CLI options take precedence).
     * If --no-config is specified, only command-line options are used.
     *
     * @param directory the directory to start searching for config files (typically the parent of the changelog file)
     * @return the merged configuration
     */
    public Config getConfigFromDirectory(@Nullable Path directory) {
        if (noConfig) {
            // Ignore config file, only use command-line options
            return getConfig();
        }
        return Config.loadFromDirectory(directory).mergeWith(getConfig());
    }
}
