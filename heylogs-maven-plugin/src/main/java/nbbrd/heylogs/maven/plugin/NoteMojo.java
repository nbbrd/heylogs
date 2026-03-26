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
@Mojo(name = "note", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true, requiresProject = false)
public final class NoteMojo extends HeylogsMojo {

    @Parameter(property = "heylogs.inputFile", defaultValue = DEFAULT_CHANGELOG_FILE)
    private File inputFile;

    @Parameter(property = "heylogs.message", required = true)
    private String message;

    @Override
    public void execute() throws MojoExecutionException {
        if (isSkip()) {
            getLog().info("Note has been skipped.");
            return;
        }

        if (!inputFile.exists()) {
            getLog().error("Changelog not found");
            throw new MojoExecutionException("Changelog not found");
        }

        Document document = readChangelog(inputFile);

        getLog().info("Setting summary: " + message);
        Heylogs.ofServiceLoader().note(document, message);

        writeChangelog(document, inputFile);
    }
}

