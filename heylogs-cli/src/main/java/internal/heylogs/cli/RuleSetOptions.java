package internal.heylogs.cli;

import nbbrd.heylogs.Rule;
import nbbrd.heylogs.RuleLoader;
import picocli.CommandLine;

import java.util.List;

@lombok.Getter
@lombok.Setter
public class RuleSetOptions {

    @CommandLine.Option(
            names = {"--semver"},
            defaultValue = "false",
            description = "Mention if this changelog follows Semantic Versioning."
    )
    private boolean semver;

    public List<Rule> getRules() {
        if (semver) {
            System.setProperty(Rule.ENABLE_KEY, "semver");
        }
        return RuleLoader.load();
    }
}
