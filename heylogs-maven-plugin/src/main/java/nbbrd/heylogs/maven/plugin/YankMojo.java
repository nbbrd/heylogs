package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.util.ast.Document;
import nbbrd.heylogs.Heylogs;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

import static internal.heylogs.HeylogsParameters.DEFAULT_CHANGELOG_FILE;

@lombok.Getter
@lombok.Setter
@Mojo(name = "yank", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true, requiresProject = false)
public final class YankMojo extends HeylogsMojo {

    @Parameter(property = "heylogs.inputFile", defaultValue = DEFAULT_CHANGELOG_FILE)
    private File inputFile;

    @Parameter(property = "heylogs.ref", required = true)
    private String ref;

    @Override
    public void execute() throws MojoExecutionException {
        if (isSkip()) {
            getLog().info("Yank has been skipped.");
            return;
        }

        if (!inputFile.exists()) {
            getLog().error("Changelog not found");
            throw new MojoExecutionException("Changelog not found");
        }

        Document document = readChangelog(inputFile);

        getLog().info("Yanking release " + ref);
        try {
            Heylogs.ofServiceLoader().yank(document, ref);
        } catch (IllegalArgumentException ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }

        writeChangelog(document, inputFile);
    }
}

