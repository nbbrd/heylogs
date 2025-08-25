package internal.heylogs.cli;

import nbbrd.heylogs.Config;
import nbbrd.heylogs.VersioningConfig;
import picocli.CommandLine;

@lombok.Getter
public class ConfigOptions {

    @CommandLine.Option(
            names = {"-t", "--tag-prefix"},
            paramLabel = "<prefix>",
            description = "Version tag prefix.",
            required = false
    )
    private String tagPrefix;

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
            description = "Specify the forge used to host the versions."
    )
    private String forgeId;

    public Config getConfig() {
        return Config
                .builder()
                .versionTagPrefix(tagPrefix)
                .versioning(versioning)
                .forgeId(forgeId)
                .build();
    }
}
