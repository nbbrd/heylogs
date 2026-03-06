package nbbrd.heylogs.maven.plugin;

import internal.heylogs.maven.plugin.MojoParameterParsing;
import lombok.NonNull;
import nbbrd.console.picocli.MultiFileInputOptions;
import nbbrd.console.picocli.text.TextOutputSupport;
import nbbrd.heylogs.Check;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.FormatConfig;
import nbbrd.heylogs.Heylogs;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

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

    @Parameter(property = "heylogs.tagging")
    private String tagging;

    @Parameter(property = "heylogs.versioning")
    private String versioning;

    @Parameter(property = "heylogs.forge")
    private String forge;

    @Parameter(property = "heylogs.rules")
    private List<String> rules;

    @Parameter(property = "heylogs.domains")
    private List<String> domains;

    @Parameter(property = "heylogs.format")
    private String format;

    @Parameter(property = "heylogs.noConfig", defaultValue = "false")
    private boolean noConfig;

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
                        .problems(heylogs.check(readChangelog(file.toFile()), config))
                        .build());
            }
        } catch (IOException ex) {
            throw new MojoExecutionException("Error while listing files", ex);
        }
        boolean hasErrors = list.stream().anyMatch(Check::hasErrors);

        TextOutputSupport outputSupport = newTextOutputSupport();
        String formatId = resolveFormatId(format != null ? FormatConfig.parse(format) : null, heylogs, outputSupport::isStdoutFile, outputFile.toPath());

        try (Writer writer = newWriter(outputFile, hasErrors ? getLog()::error : getLog()::info)) {
            heylogs.formatProblems(formatId, writer, list);
        } catch (IOException ex) {
            throw new MojoExecutionException("Error while writing", ex);
        }

        if (hasErrors)
            throw new MojoExecutionException("Invalid changelog");
    }

    @MojoParameterParsing
    private @NonNull Config toConfig() throws MojoExecutionException {
        try {
            // Build mojo parameters config
            Config mojoConfig = Config
                    .builder()
                    .taggingOf(tagging)
                    .versioningOf(versioning)
                    .forgeOf(forge)
                    .rulesOf(rules)
                    .domainsOf(domains)
                    .build();

            if (noConfig) {
                // Ignore config file, only use mojo parameters
                return mojoConfig;
            }

            // Load config from file hierarchy starting from first input file's parent or current directory
            Path firstInputFile = (inputFiles != null && !inputFiles.isEmpty()) ? inputFiles.get(0).toPath() : null;
            Config fileConfig = Config.loadFromDirectory(Config.resolveStartDir(firstInputFile));

            // Merge with mojo parameters taking precedence
            return fileConfig.mergeWith(mojoConfig);
        } catch (IllegalArgumentException ex) {
            throw new MojoExecutionException("Invalid config parameter", ex);
        }
    }
}
