package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;

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
}
