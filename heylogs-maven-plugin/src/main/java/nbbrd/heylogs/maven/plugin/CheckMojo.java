package nbbrd.heylogs.maven.plugin;

import internal.heylogs.StylishFormat;
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

import static internal.heylogs.maven.plugin.HeylogsParameters.*;
import static java.util.Locale.ROOT;
import static java.util.stream.Collectors.toList;
import static nbbrd.console.picocli.ByteOutputSupport.DEFAULT_STDOUT_FILE;

@Mojo(name = "check", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true, requiresProject = false)
public final class CheckMojo extends HeylogsMojo {

    @Parameter(defaultValue = WORKING_DIR_CHANGELOG, property = INPUT_FILES_PROPERTY)
    private List<File> inputFiles;

    @Parameter(defaultValue = "false", property = "heylogs.recursive")
    private boolean recursive;

    @Parameter(defaultValue = DEFAULT_STDOUT_FILE, property = OUTPUT_FILE_PROPERTY)
    private File outputFile;

    @Parameter(defaultValue = "false", property = "heylogs.semver")
    private boolean semver;

    @Parameter(defaultValue = StylishFormat.ID, property = FORMAT_ID_PROPERTY)
    private String formatId;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Checking has been skipped.");
            return;
        }

        if (semver) {
            checkVersioning(new SemVer(), getProjectVersionOrNull());
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
