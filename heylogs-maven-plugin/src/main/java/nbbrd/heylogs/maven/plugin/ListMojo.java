package nbbrd.heylogs.maven.plugin;

import internal.heylogs.SemverRule;
import nbbrd.heylogs.Lister;
import nbbrd.heylogs.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Mojo(name = "list", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public final class ListMojo extends HeylogsMojo {

    @Parameter(defaultValue = "false", property = "heylogs.semver")
    private boolean semver;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Listing has been skipped.");
            return;
        }

        list(loadLister());
    }

    private Lister loadLister() {
        Lister.Builder result = Lister.ofServiceLoader()
                .toBuilder();
        if (semver) {
            result.rule(new SemverRule());
        }
        return result.build();
    }

    private void list(Lister lister) throws MojoExecutionException {
        writeResources(lister);
    }

    private void writeResources(Lister lister) throws MojoExecutionException {
        try {
            StringBuilder text = new StringBuilder();
            lister.format(text, Stream.concat(
                    lister.getRules().stream().map(rule -> new Resource("rule", rule.getRuleId())),
                    lister.getFormats().stream().map(format -> new Resource("format", format.getFormatId()))
            ).collect(toList()));
            new BufferedReader(new StringReader(text.toString())).lines().forEach(getLog()::info);
        } catch (IOException ex) {
            throw new MojoExecutionException("Error while writing failures", ex);
        }
    }
}
