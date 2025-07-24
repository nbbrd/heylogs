package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.maven.plugin.MojoParameterParsing;
import lombok.NonNull;
import nbbrd.heylogs.Version;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import static internal.heylogs.HeylogsParameters.DEFAULT_CHANGELOG_FILE;
import static internal.heylogs.HeylogsParameters.DEFAULT_SEMVER;

@lombok.Getter
@lombok.Setter
@Mojo(name = "release", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true, requiresProject = false)
public final class ReleaseMojo extends HeylogsMojo {

    @Parameter(property = "heylogs.inputFile", defaultValue = DEFAULT_CHANGELOG_FILE)
    private File inputFile;

    @Parameter(property = "heylogs.ref", defaultValue = "${project.version}")
    private String ref;

    @Parameter(property = "heylogs.tagPrefix")
    private String tagPrefix;

    @Parameter(property = "heylogs.date")
    private String date;

    @Parameter(property = "heylogs.semver", defaultValue = DEFAULT_SEMVER)
    private boolean semver;

    @Override
    public void execute() throws MojoExecutionException {
        if (isSkip()) {
            getLog().info("Release has been skipped.");
            return;
        }

        if (!inputFile.exists()) {
            getLog().error("Changelog not found");
            throw new MojoExecutionException("Changelog not found");
        }

        Document document = readChangelog(inputFile);

        Version version = toVersion();
        String tagPrefix = toTagPrefix();

        getLog().info("Releasing " + version + " with tag prefix '" + tagPrefix + "'");
        initHeylogs(semver).releaseChanges(document, version, tagPrefix, semver ? "semver" : null);

        writeChangelog(document, inputFile);
    }

    @MojoParameterParsing
    private @NonNull Version toVersion() throws MojoExecutionException {
        return Version.of(ref, '-', parseLocalDate(date));
    }

    @MojoParameterParsing
    private @NonNull String toTagPrefix() {
        return Objects.toString(tagPrefix, "");
    }

    private static @NonNull LocalDate parseLocalDate(@Nullable String date) throws MojoExecutionException {
        try {
            return Version.parseLocalDate(date);
        } catch (DateTimeParseException ex) {
            throw new MojoExecutionException("Invalid format for 'date' parameter", ex);
        }
    }
}
