package nbbrd.heylogs.maven.plugin;

import internal.heylogs.StylishFormat;
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

import static internal.heylogs.maven.plugin.HeylogsParameters.*;
import static java.util.Collections.singletonList;
import static java.util.Locale.ROOT;

@Mojo(name = "check", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true, requiresProject = false)
public final class CheckMojo extends HeylogsMojo {

    @Parameter(defaultValue = WORKING_DIR_CHANGELOG, property = INPUT_FILE_PROPERTY)
    private File inputFile;

    @Parameter(defaultValue = MOJO_LOG_FILE, property = OUTPUT_FILE_PROPERTY)
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

        if (inputFile.exists()) {
            check();
        } else {
            if (isRootProject()) {
                raiseErrorMissingChangelog();
            } else {
                notifyMissingChangelog();
            }
        }
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

    private void check() throws MojoExecutionException {
        Heylogs heylogs = initHeylogs(semver);

        Check check = Check
                .builder()
                .source(inputFile.toString())
                .problems(heylogs.checkFormat(readChangelog(inputFile)))
                .build();

        try (Writer writer = newWriter(outputFile, check.hasErrors() ? getLog()::error : getLog()::info)) {
            heylogs.formatProblems(formatId, writer, singletonList(check));
        } catch (IOException ex) {
            throw new MojoExecutionException("Error while writing", ex);
        }

        if (check.hasErrors()) {
            throw new MojoExecutionException("Invalid changelog");
        }
    }
}
