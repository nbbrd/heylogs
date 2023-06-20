package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.util.ast.Document;
import nbbrd.heylogs.Extractor;
import nbbrd.heylogs.TimeRange;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.Objects;

import static internal.heylogs.maven.plugin.MojoFunction.onLocalDate;
import static internal.heylogs.maven.plugin.MojoFunction.onPattern;

@Mojo(name = "extract", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
public final class ExtractMojo extends HeylogsMojo {

    @Parameter(defaultValue = "${project.basedir}/CHANGELOG.md", property = "heylogs.input.file")
    private File inputFile;

    @Parameter(defaultValue = "${project.build.directory}/CHANGELOG.md", property = "heylogs.output.file")
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

        extract(loadExtractor());
    }

    private Extractor loadExtractor() throws MojoExecutionException {
        return Extractor
                .builder()
                .ref(Objects.toString(ref, ""))
                .unreleasedPattern(onPattern("Invalid unreleased pattern").applyWithMojo(unreleasedPattern))
                .timeRange(TimeRange.of(
                        onLocalDate("Invalid format for 'from' parameter").applyWithMojo(from),
                        onLocalDate("Invalid format for 'to' parameter").applyWithMojo(to))
                )
                .limit(limit)
                .ignoreContent(ignoreContent)
                .build();
    }

    private void extract(Extractor extractor) throws MojoExecutionException {
        Document changelog = readChangelog(inputFile);

        getLog().info("Extracting with " + extractor);
        extractor.extract(changelog);

        writeChangelog(changelog, outputFile);
    }
}
