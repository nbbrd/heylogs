package nbbrd.heylogs.maven.plugin;

import internal.heylogs.SemverRule;
import nbbrd.heylogs.Checker;
import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.Rule;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import static java.util.stream.Collectors.joining;

@Mojo(name = "list", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public final class ListMojo extends HeylogsMojo {

    @Parameter(defaultValue = "false", property = "heylogs.semver")
    private boolean semver;

    @Override
    public void execute() {
        if (skip) {
            getLog().info("Listing has been skipped.");
            return;
        }

        list(loadChecker());
    }

    private Checker loadChecker() {
        Checker.Builder result = Checker.ofServiceLoader()
                .toBuilder();
        if (semver) {
            result.rule(new SemverRule());
        }
        return result.build();
    }

    private void list(Checker checker) {
        getLog().info("Rules: " + checker.getRules().stream().map(Rule::getId).collect(joining(", ")));
        getLog().info("Formats: " + checker.getFormats().stream().map(Format::getId).collect(joining(", ")));
    }
}
