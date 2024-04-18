package nbbrd.heylogs.maven.plugin;

import internal.heylogs.semver.SemVer;
import nbbrd.heylogs.Check;
import nbbrd.heylogs.Heylogs;
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

@Mojo(name = "check", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public final class CheckMojo extends HeylogsMojo {

    @Parameter(defaultValue = DEFAULT_INPUT_FILE, property = INPUT_FILE_PROPERTY)
    private File inputFile;

    @Parameter(defaultValue = DEFAULT_OUTPUT_FILE, property = OUTPUT_FILE_PROPERTY)
    private File outputFile;

    @Parameter(defaultValue = "false", property = "heylogs.semver")
    private boolean semver;

    @Parameter(defaultValue = DEFAULT_FORMAT_ID, property = FORMAT_ID_PROPERTY)
    private String formatId;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Checking has been skipped.");
            return;
        }

        if (semver) {
            checkVersioning(new SemVer());
        }

        if (inputFile.exists()) {
            check();
        } else {
            if (isRootProject(projectBaseDir)) {
                raiseErrorMissingChangelog();
            } else {
                notifyMissingChangelog();
            }
        }
    }

    private void checkVersioning(Versioning versioning) throws MojoExecutionException {
        getLog().info("Using " + versioning.getVersioningName() + " (" + versioning.getVersioningId() + ")");
        if (versioning.isValidVersion(projectVersion)) {
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
                .problems(heylogs.validate(readChangelog(inputFile)))
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
