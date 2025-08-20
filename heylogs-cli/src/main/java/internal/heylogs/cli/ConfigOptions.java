package internal.heylogs.cli;

import nbbrd.heylogs.Config;
import picocli.CommandLine;

import java.util.Objects;

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
            paramLabel = "<id>",
            description = "Specify the versioning used to control the version references. Valid values: ${COMPLETION-CANDIDATES}.",
            completionCandidates = VersioningCandidates.class
    )
    private String versioningId;

    @CommandLine.Option(
            names = {"--versioning-arg"},
            paramLabel = "<arg>",
            description = "Specify the argument used to configure the versioning."
    )
    private String versioningArg;

    public Config getConfig() {
        return Config
                .builder()
                .versionTagPrefix(Objects.toString(getTagPrefix(), ""))
                .versioningId(versioningId)
                .versioningArg(versioningArg)
                .build();
    }
}
