package nbbrd.heylogs.maven.plugin;

import nbbrd.console.picocli.text.TextOutputSupport;
import nbbrd.heylogs.Heylogs;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import static internal.heylogs.FormatSupport.resolveFormatId;
import static internal.heylogs.HeylogsParameters.DEFAULT_SEMVER;
import static nbbrd.console.picocli.ByteOutputSupport.DEFAULT_STDOUT_FILE;
import static nbbrd.console.picocli.text.TextOutputSupport.newTextOutputSupport;

@lombok.Getter
@lombok.Setter
@Mojo(name = "list", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true, requiresProject = false)
public final class ListMojo extends HeylogsMojo {

    @Parameter(property = "heylogs.outputFile", defaultValue = DEFAULT_STDOUT_FILE)
    private File outputFile;

    @Parameter(property = "heylogs.semver", defaultValue = DEFAULT_SEMVER)
    private boolean semver;

    @Parameter(property = "heylogs.formatId")
    private String formatId;

    @Override
    public void execute() throws MojoExecutionException {
        if (isSkip()) {
            getLog().info("Listing has been skipped.");
            return;
        }

        Heylogs heylogs = initHeylogs(semver);

        TextOutputSupport outputSupport = newTextOutputSupport();
        String formatId = resolveFormatId(getFormatId(), heylogs, outputSupport::isStdoutFile, outputFile.toPath());

        try (Writer writer = newWriter(outputFile, getLog()::info)) {
            heylogs.formatResources(formatId, writer, heylogs.listResources());
        } catch (IOException ex) {
            throw new MojoExecutionException("Error while writing", ex);
        }
    }
}
