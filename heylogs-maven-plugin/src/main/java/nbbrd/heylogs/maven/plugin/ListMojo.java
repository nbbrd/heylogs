package nbbrd.heylogs.maven.plugin;

import internal.heylogs.StylishFormat;
import nbbrd.heylogs.Heylogs;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "list", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public final class ListMojo extends HeylogsMojo {

    @Parameter(defaultValue = "false", property = "heylogs.semver")
    private boolean semver;

    @Parameter(defaultValue = StylishFormat.ID, property = "heylogs.format.id")
    private String formatId;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Listing has been skipped.");
            return;
        }

        list();
    }

    private void list() throws MojoExecutionException {
        Heylogs heylogs = initHeylogs(semver);
        log(appendable -> heylogs.formatResources(formatId, appendable, heylogs.getResources()), getLog()::info);
    }
}
