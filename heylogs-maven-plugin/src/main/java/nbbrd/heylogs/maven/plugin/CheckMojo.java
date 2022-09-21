package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import nbbrd.heylogs.Failure;
import nbbrd.heylogs.Rule;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.List;

@Mojo(name = "check", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
public final class CheckMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}/CHANGELOG.md", property = "heylogs.inputFile")
    private File inputFile;

    @Parameter(defaultValue = "false", property = "heylogs.skip")
    private boolean skip;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Checking has been skipped.");
            return;
        }

        if (!inputFile.exists()) {
            getLog().info("Changelog not found");
            return;
        }

        try {
            getLog().info("Reading " + inputFile);
            Document changelog = read();

            List<Failure> failures = Failure.allOf(changelog, Rule.getDefault());
            if (!failures.isEmpty()) {
                getLog().error("Invalid CHANGELOG");
                failures.forEach(failure -> getLog().error(failure.toString()));
                throw new MojoExecutionException("Invalid CHANGELOG");
            }
            getLog().info("Valid CHANGELOG");
        } catch (IOException ex) {
            throw new MojoExecutionException("Error while checking changelog", ex);
        }
    }

    public Document read() throws IOException {
        Parser parser = Parser.builder().build();
        try (Reader reader = Files.newBufferedReader(inputFile.toPath())) {
            return parser.parseReader(reader);
        }
    }
}
