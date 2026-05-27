package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.util.ast.Document;
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
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static internal.heylogs.HeylogsParameters.DEFAULT_CHANGELOG_FILE;
import static nbbrd.heylogs.spi.FormatSupport.resolveFormatId;

@lombok.Getter
@lombok.Setter
@Mojo(name = "import", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true, requiresProject = false)
public final class ImportMojo extends HeylogsMojo {

    @Parameter(property = "heylogs.inputFile", required = true)
    private File inputFile;

    @Parameter(property = "heylogs.outputFile", defaultValue = DEFAULT_CHANGELOG_FILE)
    private File outputFile;

    @Parameter(property = "heylogs.format", defaultValue = "")
    private String format;

    @Override
    public void execute() throws MojoExecutionException {
        if (isSkip()) {
            getLog().info("Import has been skipped.");
            return;
        }

        if (!inputFile.exists()) {
            getLog().error("Input file not found");
            throw new MojoExecutionException("Input file not found");
        }

        Heylogs heylogs = Heylogs.ofServiceLoader();

        String formatId = resolveFormatId(format.isEmpty() ? null : FormatConfig.parse(format), heylogs, path -> false, inputFile.toPath(), FormatType.CONTENT);

        getLog().info("Reading changelog content with format '" + formatId + "' from " + inputFile);
        ChangelogContent content;
        try (Reader reader = Files.newBufferedReader(inputFile.toPath(), StandardCharsets.UTF_8)) {
            content = heylogs.parseContent(formatId, reader);
        } catch (IOException ex) {
            throw new MojoExecutionException("Failed to read changelog content", ex);
        }

        Document document = content.toDocument();
        writeChangelog(document, outputFile);
    }
}
