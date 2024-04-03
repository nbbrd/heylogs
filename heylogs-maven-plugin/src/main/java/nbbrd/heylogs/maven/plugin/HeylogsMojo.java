package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.SemverRule;
import nbbrd.heylogs.Heylogs;
import nbbrd.io.function.IOConsumer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;
import java.nio.file.Files;
import java.util.function.Consumer;

abstract class HeylogsMojo extends AbstractMojo {

    @Parameter(defaultValue = "false", property = "heylogs.skip")
    protected boolean skip;

    protected void raiseErrorMissingChangelog() throws MojoExecutionException {
        getLog().error("Missing changelog");
        throw new MojoExecutionException("Missing changelog");
    }

    protected void notifyMissingChangelog() {
        getLog().info("Changelog not found");
    }

    protected Document readChangelog(File inputFile) throws MojoExecutionException {
        getLog().info("Reading changelog " + inputFile);
        Parser parser = Parser.builder().build();
        try (Reader reader = Files.newBufferedReader(inputFile.toPath())) {
            return parser.parseReader(reader);
        } catch (IOException ex) {
            throw new MojoExecutionException("Failed to read changelog", ex);
        }
    }

    protected void writeChangelog(Document document, File outputFile) throws MojoExecutionException {
        getLog().info("Writing changelog " + outputFile);
        Formatter formatter = Formatter.builder().build();
        try {
            Files.createDirectories(outputFile.getParentFile().toPath());
            try (Writer writer = Files.newBufferedWriter(outputFile.toPath())) {
                formatter.render(document, writer);
            }
        } catch (IOException ex) {
            throw new MojoExecutionException("Failed to write changelog", ex);
        }
    }

    protected static boolean isRootProject(File projectBaseDir) {
        File parentDir = projectBaseDir.getParentFile();
        if (parentDir != null) {
            File parentPom = new File(parentDir, "pom.xml");
            return !parentPom.exists();
        }
        return true;
    }


    protected static Heylogs initHeylogs(boolean semver) {
        Heylogs.Builder result = Heylogs.ofServiceLoader()
                .toBuilder();
        if (semver) {
            result.rule(new SemverRule());
        }
        return result.build();
    }

    protected static void log(IOConsumer<? super Appendable> consumer, Consumer<CharSequence> logger) throws MojoExecutionException {
        try {
            StringBuilder text = new StringBuilder();
            consumer.acceptWithIO(text);
            new BufferedReader(new StringReader(text.toString())).lines().forEach(logger);
        } catch (IOException ex) {
            throw new MojoExecutionException("Error while logging", ex);
        }
    }
}
