package internal.heylogs.cli;

import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.ext.semver.SemVerRule;
import picocli.CommandLine;

@lombok.Getter
public class HeylogsOptions {

    @CommandLine.Option(
            names = {"-s", "--semver"},
            defaultValue = "false",
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
