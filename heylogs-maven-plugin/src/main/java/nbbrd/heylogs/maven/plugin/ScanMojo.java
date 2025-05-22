package nbbrd.heylogs.maven.plugin;

import nbbrd.console.picocli.MultiFileInputOptions;
import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.Scan;
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

import static internal.heylogs.HeylogsParameters.*;
import static java.util.stream.Collectors.toList;
import static nbbrd.console.picocli.ByteOutputSupport.DEFAULT_STDOUT_FILE;

@lombok.Getter
@lombok.Setter
@Mojo(name = "scan", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true, requiresProject = false)
public final class ScanMojo extends HeylogsMojo {

    @Parameter(property = "heylogs.inputFiles", defaultValue = DEFAULT_CHANGELOG_FILE)
    private List<File> inputFiles;

    @Parameter(property = "heylogs.recursive", defaultValue = DEFAULT_RECURSIVE)
    private boolean recursive;

    @Parameter(property = "heylogs.outputFile", defaultValue = DEFAULT_STDOUT_FILE)
    private File outputFile;

    @Parameter(property = "heylogs.formatId", defaultValue = DEFAULT_FORMAT_ID)
    private String formatId;

    @Override
    public void execute() throws MojoExecutionException {
        if (isSkip()) {
            getLog().info("Scanning has been skipped.");
            return;
        }

        Heylogs heylogs = initHeylogs(false);

        List<Scan> list = new ArrayList<>();
        try {
            MultiFileInputOptions input = new MultiFileInputOptions();
            input.setFiles(inputFiles.stream().map(File::toPath).collect(toList()));
            input.setRecursive(recursive);
            input.setSkipErrors(false);
            for (Path file : input.getAllFiles(HeylogsMojo::accept)) {
                list.add(Scan
                        .builder()
                        .source(file.toString())
                        .summary(heylogs.scanContent(readChangelog(file.toFile())))
                        .build());
            }
        } catch (IOException ex) {
            throw new MojoExecutionException("Error while listing files", ex);
        }

        try (Writer writer = newWriter(outputFile, getLog()::info)) {
            heylogs.formatStatus(formatId, writer, list);
        } catch (IOException ex) {
            throw new MojoExecutionException("Error while writing", ex);
        }
    }
}
