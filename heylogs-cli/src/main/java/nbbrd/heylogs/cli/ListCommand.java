package nbbrd.heylogs.cli;

import internal.heylogs.SemverRule;
import internal.heylogs.cli.SpecialProperties;
import nbbrd.heylogs.Checker;
import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.Rule;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

import static java.util.stream.Collectors.joining;

@Command(name = "list", description = "List available resources.")
public final class ListCommand implements Callable<Void> {

    @CommandLine.Option(
            names = {"-s", "--semver"},
            defaultValue = "false",
            description = "Mention if this changelog follows Semantic Versioning."
    )
    private boolean semver;

    @CommandLine.Option(
            names = {SpecialProperties.DEBUG_OPTION},
            defaultValue = "false",
            hidden = true
    )
    private boolean debug;

    @Override
    public Void call() {
        Checker checker = getChecker();
        System.out.println("Rules: " + checker.getRules().stream().map(Rule::getId).collect(joining(", ")));
        System.out.println("Formats: " + checker.getFormats().stream().map(Format::getId).collect(joining(", ")));
        return null;
    }

    private Checker getChecker() {
        Checker.Builder result = Checker.ofServiceLoader()
                .toBuilder();
        if (semver) {
            result.rule(new SemverRule());
        }
        return result.build();
    }
}
