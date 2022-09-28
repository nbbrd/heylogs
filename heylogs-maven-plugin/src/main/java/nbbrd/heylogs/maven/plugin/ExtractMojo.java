package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import nbbrd.heylogs.VersionFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Mojo(name = "extract", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
public final class ExtractMojo extends AbstractMojo {

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

    @Parameter(defaultValue = "false", property = "heylogs.skip")
    private boolean skip;

    @Parameter(defaultValue = "^.*-SNAPSHOT$", property = "heylogs.unreleased.pattern")
    private String unreleasedPattern;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Extracting has been skipped.");
            return;
        }

        if (!inputFile.exists()) {
            getLog().info("Changelog not found");
            return;
        }

        getLog().info("Reading " + inputFile);
        Document changelog = read();

        VersionFilter filter = getFilter();

        getLog().info("Extracting with " + filter);
        filter.apply(changelog);

        getLog().info("Writing " + outputFile);
        write(changelog);
    }

    private VersionFilter getFilter() throws MojoExecutionException {
        return VersionFilter
                .builder()
                .ref(Objects.toString(ref, ""))
                .unreleasedPattern(fetchUnreleasedPattern())
                .from(fetchFrom())
                .to(fetchTo())
                .limit(limit)
                .build();
    }

    private Pattern fetchUnreleasedPattern() throws MojoExecutionException {
        try {
            return Pattern.compile(unreleasedPattern);
        } catch (PatternSyntaxException ex) {
            throw new MojoExecutionException("Invalid unreleased pattern", ex);
        }
    }

    private LocalDate fetchFrom() throws MojoExecutionException {
        try {
            return VersionFilter.parseLocalDate(from);
        } catch (DateTimeParseException ex) {
            throw new MojoExecutionException("Invalid format for 'from' parameter", ex);
        }
    }

    private LocalDate fetchTo() throws MojoExecutionException {
        try {
            return VersionFilter.parseLocalDate(to);
        } catch (DateTimeParseException ex) {
            throw new MojoExecutionException("Invalid format for 'to' parameter", ex);
        }
    }

    public Document read() throws MojoExecutionException {
        Parser parser = Parser.builder().build();
        try (Reader reader = Files.newBufferedReader(inputFile.toPath())) {
            return parser.parseReader(reader);
        } catch (IOException ex) {
            throw new MojoExecutionException("Failed to read file", ex);
        }
    }

    public void write(Document document) throws MojoExecutionException {
        try {
            Files.createDirectories(outputFile.getParentFile().toPath());
            Formatter formatter = Formatter.builder().build();
            try (Writer writer = Files.newBufferedWriter(outputFile.toPath())) {
                formatter.render(document, writer);
            }
        } catch (IOException ex) {
            throw new MojoExecutionException("Failed to write file", ex);
        }
    }
}
