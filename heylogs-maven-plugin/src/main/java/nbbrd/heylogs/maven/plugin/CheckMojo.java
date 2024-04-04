package nbbrd.heylogs.maven.plugin;

import internal.heylogs.StylishFormat;
import nbbrd.heylogs.Check;
import nbbrd.heylogs.Heylogs;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.semver4j.Semver;

import java.io.File;

import static java.util.Collections.singletonList;
import static java.util.Locale.ROOT;

@Mojo(name = "check", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public final class CheckMojo extends HeylogsMojo {

    @Parameter(defaultValue = "${project.basedir}/CHANGELOG.md", property = "heylogs.input.file")
    private File inputFile;

    @Parameter(defaultValue = "false", property = "heylogs.semver")
    private boolean semver;

    @Parameter(defaultValue = StylishFormat.ID, property = "heylogs.format.id")
    private String formatId;

    @Parameter(defaultValue = "${project.version}", readonly = true)
    private String projectVersion;

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File projectBaseDir;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Checking has been skipped.");
            return;
        }

        if (semver) {
            checkSemanticVersioning();
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

    private void checkSemanticVersioning() throws MojoExecutionException {
        getLog().info("Using Semantic Versioning specification");
        if (Semver.isValid(projectVersion)) {
            getLog().info("Valid project version");
        } else {
            getLog().error(String.format(ROOT, "Invalid project version: '%s' must follow Semantic Versioning specification (https://semver.org/)", projectVersion));
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
        log(text -> heylogs.formatProblems(formatId, text, singletonList(check)), check.hasErrors() ? getLog()::error : getLog()::info);
        if (check.hasErrors()) {
            throw new MojoExecutionException("Invalid changelog");
        }
    }
}
