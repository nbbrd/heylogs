package nbbrd.heylogs.maven.plugin;

import internal.heylogs.StylishFormat;
import nbbrd.heylogs.Heylogs;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import static internal.heylogs.maven.plugin.HeylogsParameters.*;

@Mojo(name = "list", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true, requiresProject = false)
public final class ListMojo extends HeylogsMojo {

    @Parameter(defaultValue = MOJO_LOG_FILE, property = OUTPUT_FILE_PROPERTY)
    private File outputFile;

    @Parameter(defaultValue = "false", property = "heylogs.semver")
    private boolean semver;

    @Parameter(defaultValue = StylishFormat.ID, property = FORMAT_ID_PROPERTY)
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

        try (Writer writer = newWriter(outputFile, getLog()::info)) {
            heylogs.formatResources(formatId, writer, heylogs.getResources());
        } catch (IOException ex) {
            throw new MojoExecutionException("Error while writing", ex);
        }
    }
}
