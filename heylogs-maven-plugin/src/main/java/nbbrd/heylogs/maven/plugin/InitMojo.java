package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.util.ast.Document;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.Heylogs;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static internal.heylogs.HeylogsParameters.DEFAULT_CHANGELOG_FILE;

@lombok.Getter
@lombok.Setter
@Mojo(name = "init", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true, requiresProject = false)
public final class InitMojo extends ConfigMojo {

    @Parameter(property = "heylogs.outputFile", defaultValue = DEFAULT_CHANGELOG_FILE)
    private File outputFile;

    @Parameter(property = "heylogs.templateFile")
    private File templateFile;

    @Parameter(property = "heylogs.projectUrl")
    private URL projectUrl;

    @Override
    public void execute() throws MojoExecutionException {
        if (isSkip()) {
            getLog().info("Init has been skipped.");
            return;
        }

        if (outputFile.exists()) {
            getLog().info("Changelog already exists: " + outputFile);
            return;
        }

        Config config = toConfig(outputFile.toPath());

        getLog().info("Initializing changelog " + outputFile);
        try {
            String template = templateFile != null ? new String(Files.readAllBytes(templateFile.toPath()), StandardCharsets.UTF_8) : null;
            Document document = Heylogs.ofServiceLoader().init(config, template, projectUrl);
            writeChangelog(document, outputFile);
        } catch (IOException ex) {
            throw new MojoExecutionException("Failed to read template file", ex);
        }
    }
}
