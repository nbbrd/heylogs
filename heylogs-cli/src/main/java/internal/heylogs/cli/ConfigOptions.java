package internal.heylogs.cli;

import nbbrd.heylogs.Config;
import picocli.CommandLine;

import java.util.Objects;

import static internal.heylogs.HeylogsParameters.DEFAULT_SEMVER;

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
            names = {"-s", "--semver"},
            defaultValue = DEFAULT_SEMVER,
            description = "Mention if this changelog follows Semantic Versioning."
    )
    private boolean semver;

    public Config getConfig() {
        return Config
                .builder()
                .versionTagPrefix(Objects.toString(getTagPrefix(), ""))
                .versioningId(isSemver() ? "semver" : null)
                .build();
    }
}
