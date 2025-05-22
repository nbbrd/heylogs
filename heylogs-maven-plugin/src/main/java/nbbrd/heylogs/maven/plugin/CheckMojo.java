package nbbrd.heylogs.maven.plugin;

import nbbrd.console.picocli.MultiFileInputOptions;
import nbbrd.heylogs.Check;
import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.ext.semver.SemVer;
import nbbrd.heylogs.spi.Versioning;
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
import static java.util.Locale.ROOT;
import static java.util.stream.Collectors.toList;
import static nbbrd.console.picocli.ByteOutputSupport.DEFAULT_STDOUT_FILE;

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

    @Parameter(property = "heylogs.semver", defaultValue = DEFAULT_SEMVER)
    private boolean semver;

    @Parameter(property = "heylogs.formatId", defaultValue = DEFAULT_FORMAT_ID)
    private String formatId;

    @Override
    public void execute() throws MojoExecutionException {
        if (isSkip()) {
            getLog().info("Checking has been skipped.");
            return;
        }

        if (semver) {
            checkVersioning(new SemVer(), getProjectVersion());
        }

        Heylogs heylogs = initHeylogs(semver);

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
                        .problems(heylogs.checkFormat(readChangelog(file.toFile())))
                        .build());
            }
        } catch (IOException ex) {
            throw new MojoExecutionException("Error while listing files", ex);
        }
        boolean hasErrors = list.stream().anyMatch(Check::hasErrors);

        try (Writer writer = newWriter(outputFile, hasErrors ? getLog()::error : getLog()::info)) {
            heylogs.formatProblems(formatId, writer, list);
        } catch (IOException ex) {
            throw new MojoExecutionException("Error while writing", ex);
        }

        if (hasErrors)
            throw new MojoExecutionException("Invalid changelog");
    }

    private void checkVersioning(Versioning versioning, String projectVersion) throws MojoExecutionException {
        getLog().info("Using " + versioning.getVersioningName() + " (" + versioning.getVersioningId() + ")");
        if (projectVersion == null) {
            getLog().warn("Cannot find project version");
        } else if (versioning.isValidVersion(projectVersion)) {
            getLog().info("Valid project version");
        } else {
            getLog().error(String.format(ROOT, "Invalid project version: '%s' must follow %s", projectVersion, versioning.getVersioningName()));
            throw new MojoExecutionException("Invalid project version. See above for details.");
        }
    }
}
