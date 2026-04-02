package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.util.ast.Document;
import nbbrd.console.picocli.MultiFileInputOptions;
import nbbrd.heylogs.Heylogs;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static internal.heylogs.HeylogsParameters.DEFAULT_CHANGELOG_FILE;
import static internal.heylogs.HeylogsParameters.DEFAULT_RECURSIVE;
import static java.util.stream.Collectors.toList;

@lombok.Getter
@lombok.Setter
@Mojo(name = "format", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true, requiresProject = false)
public final class FormatMojo extends HeylogsMojo {

    @Parameter(property = "heylogs.inputFiles", defaultValue = DEFAULT_CHANGELOG_FILE)
    private List<File> inputFiles;

    @Parameter(property = "heylogs.recursive", defaultValue = DEFAULT_RECURSIVE)
    private boolean recursive;

    @Parameter(property = "heylogs.check", defaultValue = "false")
    private boolean check;

    @Override
    public void execute() throws MojoExecutionException {
        if (isSkip()) {
            getLog().info("Format has been skipped.");
            return;
        }

        Heylogs heylogs = Heylogs.ofServiceLoader();

        try {
            MultiFileInputOptions input = new MultiFileInputOptions();
            input.setFiles(inputFiles.stream().map(File::toPath).collect(toList()));
            input.setRecursive(recursive);
            input.setSkipErrors(false);
            for (Path file : input.getAllFiles(HeylogsMojo::accept)) {
                if (check) {
                    getLog().info("Checking changelog format: " + file);
                    if (heylogs.format(readChangelog(file.toFile()))) {
                        throw new MojoExecutionException("Changelog is not properly formatted: " + file);
                    }
                } else {
                    getLog().info("Formatting changelog: " + file);
                    Document document = readChangelog(file.toFile());
                    heylogs.format(document);
                    writeChangelog(document, file.toFile());
                }
            }
        } catch (MojoExecutionException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new MojoExecutionException("Error while listing files", ex);
        }
    }
}

