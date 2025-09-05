package nbbrd.heylogs.maven.plugin;

import nbbrd.console.picocli.text.TextOutputSupport;
import nbbrd.heylogs.FormatConfig;
import nbbrd.heylogs.Heylogs;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import static internal.heylogs.spi.FormatSupport.resolveFormatId;
import static nbbrd.console.picocli.ByteOutputSupport.DEFAULT_STDOUT_FILE;
import static nbbrd.console.picocli.text.TextOutputSupport.newTextOutputSupport;

@lombok.Getter
@lombok.Setter
@Mojo(name = "list", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true, requiresProject = false)
public final class ListMojo extends HeylogsMojo {

    @Parameter(property = "heylogs.outputFile", defaultValue = DEFAULT_STDOUT_FILE)
    private File outputFile;

    @Parameter(property = "heylogs.format")
    private String format;

    @Override
    public void execute() throws MojoExecutionException {
        if (isSkip()) {
            getLog().info("Listing has been skipped.");
            return;
        }

        Heylogs heylogs = Heylogs.ofServiceLoader();

        TextOutputSupport outputSupport = newTextOutputSupport();
        String formatId = resolveFormatId(format != null ? FormatConfig.parse(format) : null, heylogs, outputSupport::isStdoutFile, outputFile.toPath());

        try (Writer writer = newWriter(outputFile, getLog()::info)) {
            heylogs.formatResources(formatId, writer, heylogs.list());
        } catch (IOException ex) {
            throw new MojoExecutionException("Error while writing", ex);
        }
    }
}
