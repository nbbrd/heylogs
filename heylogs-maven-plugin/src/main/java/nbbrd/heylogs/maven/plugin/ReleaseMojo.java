package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.maven.plugin.MojoParameterParsing;
import lombok.NonNull;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.Heylogs;
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

@lombok.Getter
@lombok.Setter
@Mojo(name = "release", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true, requiresProject = false)
public final class ReleaseMojo extends HeylogsMojo {

    @Parameter(property = "heylogs.inputFile", defaultValue = DEFAULT_CHANGELOG_FILE)
    private File inputFile;

    @Parameter(property = "heylogs.ref", defaultValue = "${project.version}")
    private String ref;

    @Parameter(property = "heylogs.date")
    private String date;

    @Parameter(property = "heylogs.tagPrefix")
    private String tagPrefix;

    @Parameter(property = "heylogs.versioningId")
    private String versioningId;

    @Parameter(property = "heylogs.versioningArg")
    private String versioningArg;

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
        Config config = toConfig();

        getLog().info("Releasing " + version + " with config '" + config + "'");
        Heylogs.ofServiceLoader().releaseChanges(document, version, config);

        writeChangelog(document, inputFile);
    }

    @MojoParameterParsing
    private @NonNull Version toVersion() throws MojoExecutionException {
        return Version.of(ref, '-', parseLocalDate(date));
    }

    @MojoParameterParsing
    private @NonNull Config toConfig() {
        return Config
                .builder()
                .versionTagPrefix(Objects.toString(tagPrefix, ""))
                .versioningId(versioningId)
                .versioningArg(versioningArg)
                .build();
    }

    private static @NonNull LocalDate parseLocalDate(@Nullable String date) throws MojoExecutionException {
        try {
            return Version.parseLocalDate(date);
        } catch (DateTimeParseException ex) {
            throw new MojoExecutionException("Invalid format for 'date' parameter", ex);
        }
    }
}
