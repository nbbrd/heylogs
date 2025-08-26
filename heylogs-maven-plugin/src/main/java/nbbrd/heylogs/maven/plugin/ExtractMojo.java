package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.maven.plugin.MojoParameterParsing;
import lombok.NonNull;
import nbbrd.heylogs.Filter;
import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.TimeRange;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static internal.heylogs.HeylogsParameters.DEFAULT_CHANGELOG_FILE;
import static nbbrd.console.picocli.ByteOutputSupport.DEFAULT_STDOUT_FILE;

@lombok.Getter
@lombok.Setter
@Mojo(name = "extract", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true, requiresProject = false)
public final class ExtractMojo extends HeylogsMojo {

    @Parameter(property = "heylogs.inputFile", defaultValue = DEFAULT_CHANGELOG_FILE)
    private File inputFile;

    @Parameter(property = "heylogs.outputFile", defaultValue = DEFAULT_STDOUT_FILE)
    private File outputFile;

    @Parameter(property = "heylogs.ref", defaultValue = "${project.version}")
    private String ref;

    @Parameter(property = "heylogs.from", defaultValue = "-999999999-01-01")
    private String from;

    @Parameter(property = "heylogs.to", defaultValue = "+999999999-12-31")
    private String to;

    @Parameter(property = "heylogs.limit", defaultValue = "0x7fffffff")
    private int limit;

    @Parameter(property = "heylogs.unreleasedPattern", defaultValue = "^.*-SNAPSHOT$")
    private String unreleasedPattern;

    @Parameter(property = "heylogs.ignoreContent", defaultValue = "false")
    private boolean ignoreContent;

    @Override
    public void execute() throws MojoExecutionException {
        if (isSkip()) {
            getLog().info("Extracting has been skipped.");
            return;
        }

        if (!inputFile.exists()) {
            getLog().error("Changelog not found");
            throw new MojoExecutionException("Changelog not found");
        }

        Document changelog = readChangelog(inputFile);

        Filter filter = toFilter();

        getLog().info("Extracting with " + filter);
        Heylogs.ofServiceLoader().extractVersions(changelog, filter);

        writeChangelog(changelog, outputFile);
    }

    @MojoParameterParsing
    private @NonNull Filter toFilter() throws MojoExecutionException {
        try {
            return Filter
                    .builder()
                    .ref(Objects.toString(ref, ""))
                    .unreleasedPattern(Pattern.compile(unreleasedPattern))
                    .timeRange(TimeRange.of(parseLocalDate(from), parseLocalDate(to)))
                    .limit(limit)
                    .ignoreContent(ignoreContent)
                    .build();
        } catch (PatternSyntaxException ex) {
            throw new MojoExecutionException("Invalid unreleased pattern", ex);
        }
    }

    private static @NonNull LocalDate parseLocalDate(@Nullable String text) throws MojoExecutionException {
        if (text == null) throw new MojoExecutionException("Missing date");
        try {
            return Filter.parseLocalDate(text);
        } catch (DateTimeParseException ex) {
            throw new MojoExecutionException("Invalid date format", ex);
        }
    }
}
