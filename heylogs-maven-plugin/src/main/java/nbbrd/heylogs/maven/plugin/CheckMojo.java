package nbbrd.heylogs.maven.plugin;

import internal.heylogs.maven.plugin.MojoParameterParsing;
import lombok.NonNull;
import nbbrd.console.picocli.MultiFileInputOptions;
import nbbrd.console.picocli.text.TextOutputSupport;
import nbbrd.heylogs.Check;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.VersioningConfig;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static internal.heylogs.HeylogsParameters.DEFAULT_CHANGELOG_FILE;
import static internal.heylogs.HeylogsParameters.DEFAULT_RECURSIVE;
import static internal.heylogs.spi.FormatSupport.resolveFormatId;
import static java.util.stream.Collectors.toList;
import static nbbrd.console.picocli.ByteOutputSupport.DEFAULT_STDOUT_FILE;
import static nbbrd.console.picocli.text.TextOutputSupport.newTextOutputSupport;

@lombok.Getter
@lombok.Setter
@Mojo(name = "check", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true, requiresProject = false)
public final class CheckMojo extends HeylogsMojo {

    @Parameter(property = "heylogs.inputFiles", defaultValue = DEFAULT_CHANGELOG_FILE)
    private List<File> inputFiles;

    @Parameter(property = "heylogs.recursive", defaultValue = DEFAULT_RECURSIVE)
    private boolean recursive;

    @Parameter(property = "heylogs.outputFile", defaultValue = DEFAULT_STDOUT_FILE)
    private File outputFile;

    @Parameter(property = "heylogs.tagPrefix")
    private String tagPrefix;

    @Parameter(property = "heylogs.versioning")
    private String versioning;

    @Parameter(property = "heylogs.forgeId")
    private String forgeId;

    @Parameter(property = "heylogs.formatId")
    private String formatId;

    @Override
    public void execute() throws MojoExecutionException {
        if (isSkip()) {
            getLog().info("Checking has been skipped.");
            return;
        }

        Config config = toConfig();
        Heylogs heylogs = Heylogs.ofServiceLoader();

        List<Check> list = new ArrayList<>();
        try {
            MultiFileInputOptions input = new MultiFileInputOptions();
            input.setFiles(inputFiles.stream().map(File::toPath).collect(toList()));
            input.setRecursive(recursive);
            input.setSkipErrors(false);
            for (Path file : input.getAllFiles(HeylogsMojo::accept)) {
                list.add(Check
                        .builder()
                        .source(file.toString())
                        .problems(heylogs.checkFormat(readChangelog(file.toFile()), config))
                        .build());
            }
        } catch (IOException ex) {
            throw new MojoExecutionException("Error while listing files", ex);
        }
        boolean hasErrors = list.stream().anyMatch(Check::hasErrors);

        TextOutputSupport outputSupport = newTextOutputSupport();
        String formatId = resolveFormatId(getFormatId(), heylogs, outputSupport::isStdoutFile, outputFile.toPath());

        try (Writer writer = newWriter(outputFile, hasErrors ? getLog()::error : getLog()::info)) {
            heylogs.formatProblems(formatId, writer, list);
        } catch (IOException ex) {
            throw new MojoExecutionException("Error while writing", ex);
        }

        if (hasErrors)
            throw new MojoExecutionException("Invalid changelog");
    }

    @MojoParameterParsing
    private @Nullable VersioningConfig toVersioningConfig() throws MojoExecutionException {
        try {
            return versioning != null ? VersioningConfig.parse(versioning) : null;
        } catch (IllegalArgumentException ex) {
            throw new MojoExecutionException("Invalid format for 'versioning' parameter", ex);
        }
    }

    @MojoParameterParsing
    private @NonNull Config toConfig() throws MojoExecutionException {
        return Config
                .builder()
                .versionTagPrefix(tagPrefix)
                .versioning(toVersioningConfig())
                .forgeId(forgeId)
                .build();
    }
}
