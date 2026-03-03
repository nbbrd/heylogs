package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.util.ast.Document;
import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.TypeOfChange;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.Locale;

import static internal.heylogs.HeylogsParameters.DEFAULT_CHANGELOG_FILE;

@lombok.Getter
@lombok.Setter
@Mojo(name = "push", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true, requiresProject = false)
public final class PushMojo extends HeylogsMojo {

    @Parameter(property = "heylogs.inputFile", defaultValue = DEFAULT_CHANGELOG_FILE)
    private File inputFile;

    @Parameter(property = "heylogs.type", required = true)
    private String type;

    @Parameter(property = "heylogs.message", required = true)
    private String message;

    @Override
    public void execute() throws MojoExecutionException {
        if (isSkip()) {
            getLog().info("Push has been skipped.");
            return;
        }

        if (!inputFile.exists()) {
            getLog().error("Changelog not found");
            throw new MojoExecutionException("Changelog not found");
        }

        Document document = readChangelog(inputFile);

        TypeOfChange typeOfChange = parseTypeOfChange(type);

        getLog().info("Pushing " + typeOfChange.getLabel() + " change: " + message);
        Heylogs.ofServiceLoader().push(document, typeOfChange, message);

        writeChangelog(document, inputFile);
    }

    private static TypeOfChange parseTypeOfChange(String type) throws MojoExecutionException {
        if (type == null || type.isEmpty()) {
            throw new MojoExecutionException("Missing 'type' parameter");
        }
        try {
            return TypeOfChange.valueOf(type.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new MojoExecutionException("Invalid type of change: '" + type + "'. Valid values: Added, Changed, Deprecated, Removed, Fixed, Security", ex);
        }
    }
}

