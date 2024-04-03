package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.StylishFormat;
import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.Problem;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.semver4j.Semver;

import java.io.File;
import java.util.List;

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
        Document changelog = readChangelog(inputFile);
        List<Problem> problems = heylogs.validate(changelog);
        log(text -> heylogs.formatProblems(formatId, text, inputFile.toString(), problems), !problems.isEmpty() ? getLog()::error : getLog()::info);
        if (Problem.hasErrors(problems)) {
            throw new MojoExecutionException("Invalid changelog");
        }
    }
}
