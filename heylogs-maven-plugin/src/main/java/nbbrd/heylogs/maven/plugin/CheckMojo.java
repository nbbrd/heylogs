package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.SemverRule;
import internal.heylogs.StylishFormat;
import nbbrd.heylogs.Checker;
import nbbrd.heylogs.Failure;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.semver4j.Semver;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

import static java.util.Locale.ROOT;

@Mojo(name = "check", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public final class CheckMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}/CHANGELOG.md", property = "heylogs.input.file")
    private File inputFile;

    @Parameter(defaultValue = "false", property = "heylogs.skip")
    private boolean skip;

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
            validateFile();
        } else {
            if (isRootProject()) {
                raiseErrorMissingFile();
            } else {
                notifyMissingFile();
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

    private void validateFile() throws MojoExecutionException {
        try {
            getLog().info("Reading " + inputFile);
            Document changelog = read();

            Checker checker = getChecker();

            List<Failure> failures = checker.validate(changelog);
            if (!failures.isEmpty()) {
                getLog().error("Invalid changelog");
                StringBuilder text = new StringBuilder();
                checker.formatFailures(text, inputFile.toString(), failures);
                new BufferedReader(new StringReader(text.toString())).lines().forEach(getLog()::error);
                throw new MojoExecutionException("Invalid changelog");
            }
            getLog().info("Valid changelog");
        } catch (IOException ex) {
            throw new MojoExecutionException("Error while checking changelog", ex);
        }
    }

    private Checker getChecker() {
        Checker.Builder result = Checker.ofServiceLoader().
                toBuilder()
                .formatId(formatId);
        if (semver) {
            result.rule(new SemverRule());
        }
        return result.build();
    }

    private boolean isRootProject() {
        File parentDir = projectBaseDir.getParentFile();
        if (parentDir != null) {
            File parentPom = new File(parentDir, "pom.xml");
            return !parentPom.exists();
        }
        return true;
    }

    private void raiseErrorMissingFile() throws MojoExecutionException {
        getLog().error("Missing changelog");
        throw new MojoExecutionException("Missing changelog");
    }

    private void notifyMissingFile() {
        getLog().info("Changelog not found");
    }

    public Document read() throws IOException {
        Parser parser = Parser.builder().build();
        try (Reader reader = Files.newBufferedReader(inputFile.toPath())) {
            return parser.parseReader(reader);
        }
    }
}
