package internal.heylogs.cli;

import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.ext.semver.SemVerRule;
import picocli.CommandLine;

import static internal.heylogs.HeylogsParameters.DEFAULT_SEMVER;

@lombok.Getter
public class HeylogsOptions {

    @CommandLine.Option(
            names = {"-s", "--semver"},
            defaultValue = DEFAULT_SEMVER,
            description = "Mention if this changelog follows Semantic Versioning."
    )
    private boolean semver;

    public Heylogs initHeylogs() {
        Heylogs.Builder result = Heylogs.ofServiceLoader()
                .toBuilder();
        if (semver) {
            result.rule(new SemVerRule());
        }
        return result.build();
    }
}
