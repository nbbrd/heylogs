package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.maven.plugin.MojoFunction;
import nbbrd.heylogs.Filter;
import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.TimeRange;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.time.LocalDate;
import java.util.Objects;
import java.util.regex.Pattern;

import static internal.heylogs.maven.plugin.HeylogsParameters.*;
import static internal.heylogs.maven.plugin.MojoFunction.of;

@Mojo(name = "extract", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true, requiresProject = false)
public final class ExtractMojo extends HeylogsMojo {

    @Parameter(defaultValue = WORKING_DIR_CHANGELOG, property = INPUT_FILE_PROPERTY)
    private File inputFile;

    @Parameter(defaultValue = "${project.build.directory}/CHANGELOG.md", property = OUTPUT_FILE_PROPERTY)
    private File outputFile;

    @Parameter(defaultValue = "${project.version}", property = "heylogs.ref")
    private String ref;

    @Parameter(defaultValue = "-999999999-01-01", property = "heylogs.from")
    private String from;

    @Parameter(defaultValue = "+999999999-12-31", property = "heylogs.to")
    private String to;

    @Parameter(defaultValue = "0x7fffffff", property = "heylogs.limit")
    private int limit;

    @Parameter(defaultValue = "^.*-SNAPSHOT$", property = "heylogs.unreleased.pattern")
    private String unreleasedPattern;

    @Parameter(defaultValue = "false", property = "heylogs.ignore.content")
    private boolean ignoreContent;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Extracting has been skipped.");
            return;
        }

        if (!inputFile.exists()) {
            getLog().error("Changelog not found");
            throw new MojoExecutionException("Changelog not found");
        }

        extract(loadFilter());
    }

    private Filter loadFilter() throws MojoExecutionException {
        return Filter
                .builder()
                .ref(Objects.toString(ref, ""))
                .unreleasedPattern(UNRELEASED_PATTERN_PARSER.applyWithMojo(unreleasedPattern))
                .timeRange(TimeRange.of(FROM_PARSER.applyWithMojo(from), TO_PARSER.applyWithMojo(to)))
                .limit(limit)
                .ignoreContent(ignoreContent)
                .build();
    }

    private void extract(Filter filter) throws MojoExecutionException {
        Heylogs heylogs = initHeylogs(false);

        Document changelog = readChangelog(inputFile);

        getLog().info("Extracting with " + filter);
        heylogs.extractVersions(changelog, filter);

        writeChangelog(changelog, outputFile);
    }

    private static final MojoFunction<String, Pattern> UNRELEASED_PATTERN_PARSER = of(Pattern::compile, "Invalid unreleased pattern");
    private static final MojoFunction<String, LocalDate> FROM_PARSER = of(Filter::parseLocalDate, "Invalid format for 'from' parameter");
    private static final MojoFunction<String, LocalDate> TO_PARSER = of(Filter::parseLocalDate, "Invalid format for 'to' parameter");
}
