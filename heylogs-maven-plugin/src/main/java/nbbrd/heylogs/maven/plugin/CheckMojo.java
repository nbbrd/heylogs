package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.SemverRule;
import internal.heylogs.StylishFormat;
import nbbrd.heylogs.Checker;
import nbbrd.heylogs.Failure;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.semver4j.Semver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
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
            check(loadChecker());
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

    private Checker loadChecker() {
        Checker.Builder result = Checker.ofServiceLoader().
                toBuilder()
                .formatId(formatId);
        if (semver) {
            result.rule(new SemverRule());
        }
        return result.build();
    }

    private void check(Checker checker) throws MojoExecutionException {
        Document changelog = readChangelog(inputFile);
        List<Failure> failures = checker.validate(changelog);
        writeFailures(checker, failures);
        if (!failures.isEmpty()) {
            throw new MojoExecutionException("Invalid changelog");
        }
    }

    private void writeFailures(Checker checker, List<Failure> failures) throws MojoExecutionException {
        try {
            StringBuilder text = new StringBuilder();
            checker.formatFailures(text, inputFile.toString(), failures);
            new BufferedReader(new StringReader(text.toString())).lines().forEach(!failures.isEmpty() ? getLog()::error : getLog()::info);
        } catch (IOException ex) {
            throw new MojoExecutionException("Error while writing failures", ex);
        }
    }
}
