package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.FlexmarkIO;
import nbbrd.design.MightBePromoted;
import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.ext.semver.SemVerRule;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;
import java.nio.file.Files;
import java.util.function.Consumer;

import static internal.heylogs.maven.plugin.HeylogsParameters.isMojoLogFile;
import static java.nio.charset.StandardCharsets.UTF_8;
import static nbbrd.console.picocli.text.TextOutputSupport.newTextOutputSupport;

abstract class HeylogsMojo extends AbstractMojo {

    @Parameter(defaultValue = "false", property = "heylogs.skip")
    protected boolean skip;

    @Parameter(defaultValue = "${project.version}", readonly = true)
    private String projectVersion;

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File projectBaseDir;

    protected String getProjectVersionOrNull() {
        return projectVersion;
    }

    protected void raiseErrorMissingChangelog() throws MojoExecutionException {
        getLog().error("Missing changelog");
        throw new MojoExecutionException("Missing changelog");
    }

    protected void notifyMissingChangelog() {
        getLog().info("Changelog not found");
    }

    protected Document readChangelog(File inputFile) throws MojoExecutionException {
        getLog().info("Reading changelog " + inputFile);
        try {
            return FlexmarkIO.newTextParser().parseFile(inputFile, UTF_8);
        } catch (IOException ex) {
            throw new MojoExecutionException("Failed to read changelog", ex);
        }
    }

    protected void writeChangelog(Document document, File outputFile) throws MojoExecutionException {
        getLog().info("Writing changelog " + outputFile);
        try {
            Files.createDirectories(outputFile.getParentFile().toPath());
            FlexmarkIO.newTextFormatter().formatFile(document, outputFile, UTF_8);
        } catch (IOException ex) {
            throw new MojoExecutionException("Failed to write changelog", ex);
        }
    }

    protected boolean isRootProject() {
        if (projectBaseDir == null) {
            return true;
        }
        File parentDir = projectBaseDir.getParentFile();
        if (parentDir != null) {
            File parentPom = parentDir.toPath().resolve("pom.xml").toFile();
            return !parentPom.exists();
        }
        return true;
    }


    protected static Heylogs initHeylogs(boolean semver) {
        Heylogs.Builder result = Heylogs.ofServiceLoader()
                .toBuilder();
        if (semver) {
            result.rule(new SemVerRule());
        }
        return result.build();
    }

    @MightBePromoted
    protected static Writer newWriter(File outputFile, Consumer<CharSequence> logger) throws IOException {
        return isMojoLogFile(outputFile)
                ? new MojoLogWriter(logger)
                : newTextOutputSupport().newBufferedWriter(outputFile.toPath());
    }

    @MightBePromoted
    @lombok.AllArgsConstructor
    private static final class MojoLogWriter extends StringWriter {

        private final Consumer<CharSequence> logger;

        @Override
        public void close() throws IOException {
            super.close();
            new BufferedReader(new StringReader(toString())).lines().forEach(logger);
        }
    }
}
