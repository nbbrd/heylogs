package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.maven.plugin.MojoFunction;
import nbbrd.design.MightBePromoted;
import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.Version;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

import static internal.heylogs.maven.plugin.HeylogsParameters.*;
import static internal.heylogs.maven.plugin.MojoFunction.of;

@Mojo(name = "release", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true, requiresProject = false)
public final class ReleaseMojo extends HeylogsMojo {

    @Parameter(defaultValue = WORKING_DIR_CHANGELOG, property = INPUT_FILE_PROPERTY)
    private File inputFile;

    @Parameter(defaultValue = "${project.build.directory}/CHANGELOG.md", property = OUTPUT_FILE_PROPERTY)
    private File outputFile;

    @Parameter(defaultValue = "${project.version}", property = "heylogs.ref")
    private String ref;

    @Parameter(defaultValue = "", property = "heylogs.tag.prefix")
    private String tagPrefix;

    @Parameter(defaultValue = "", property = "heylogs.date")
    private String date;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Release has been skipped.");
            return;
        }

        if (!inputFile.exists()) {
            getLog().error("Changelog not found");
            throw new MojoExecutionException("Changelog not found");
        }

        release(loadVersion(), loadTagPrefix());
    }

    private Version loadVersion() throws MojoExecutionException {
        return Version.of(ref, '-', DATE_PARSER.applyWithMojo(date));
    }

    private String loadTagPrefix() throws MojoExecutionException {
        return TAG_PREFIX_PARSER.applyWithMojo(tagPrefix);
    }

    private void release(Version version, String tagPrefix) throws MojoExecutionException {
        Heylogs heylogs = initHeylogs(false);

        Document changelog = readChangelog(inputFile);

        getLog().info("Releasing " + version + " with tag prefix '" + tagPrefix + "'");
        heylogs.releaseChanges(changelog, version, tagPrefix);

        writeChangelog(changelog, outputFile);
    }

    @MightBePromoted
    private static String parseTagPrefix(String tagPrefix) {
        return Objects.toString(tagPrefix, "");
    }

    @MightBePromoted
    private static LocalDate parseDate(String date) {
        return date == null ? LocalDate.now(ZoneId.systemDefault()) : LocalDate.parse(date);
    }

    private static final MojoFunction<String, String> TAG_PREFIX_PARSER = of(ReleaseMojo::parseTagPrefix, "Invalid format for 'tagPrefix' parameter");
    private static final MojoFunction<String, LocalDate> DATE_PARSER = of(ReleaseMojo::parseDate, "Invalid format for 'date' parameter");
}
