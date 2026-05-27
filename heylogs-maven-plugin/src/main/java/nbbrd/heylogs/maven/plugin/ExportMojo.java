package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.util.ast.Document;
import nbbrd.console.picocli.text.TextOutputSupport;
import nbbrd.heylogs.ChangelogContent;
import nbbrd.heylogs.FormatConfig;
import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.spi.FormatType;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import static internal.heylogs.HeylogsParameters.DEFAULT_CHANGELOG_FILE;
import static nbbrd.console.picocli.ByteOutputSupport.DEFAULT_STDOUT_FILE;
import static nbbrd.console.picocli.text.TextOutputSupport.newTextOutputSupport;
import static nbbrd.heylogs.spi.FormatSupport.resolveFormatId;

@lombok.Getter
@lombok.Setter
@Mojo(name = "export", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true, requiresProject = false)
public final class ExportMojo extends HeylogsMojo {

    @Parameter(property = "heylogs.inputFile", defaultValue = DEFAULT_CHANGELOG_FILE)
    private File inputFile;

    @Parameter(property = "heylogs.outputFile", defaultValue = DEFAULT_STDOUT_FILE)
    private File outputFile;

    @Parameter(property = "heylogs.format", defaultValue = "")
    private String format;

    @Override
    public void execute() throws MojoExecutionException {
        if (isSkip()) {
            getLog().info("Export has been skipped.");
            return;
        }

        if (!inputFile.exists()) {
            getLog().error("Changelog not found");
            throw new MojoExecutionException("Changelog not found");
        }

        Heylogs heylogs = Heylogs.ofServiceLoader();

        Document document = readChangelog(inputFile);

        ChangelogContent content = heylogs.content(document)
                .orElseThrow(() -> new MojoExecutionException("No changelog found in " + inputFile));

        String formatId = resolveFormatId(format.isEmpty() ? null : FormatConfig.parse(format), heylogs, newTextOutputSupport()::isStdoutFile, outputFile.toPath(), FormatType.CONTENT);

        getLog().info("Writing changelog content with format '" + formatId + "' to " + outputFile);
        try (Writer writer = newWriter(outputFile, getLog()::info)) {
            heylogs.formatContent(formatId, writer, content);
        } catch (IOException ex) {
            throw new MojoExecutionException("Failed to write changelog content", ex);
        }
    }
}
