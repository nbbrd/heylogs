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

@Mojo(name = "extract", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
public final class ExtractMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}/CHANGELOG.md", property = "heylogs.inputFile")
    private File inputFile;

    @Parameter(defaultValue = "${project.build.directory}/CHANGELOG.md", property = "heylogs.outputFile")
    private File outputFile;

    @Parameter(defaultValue = "", property = "heylogs.ref")
    private String ref;

    @Parameter(defaultValue = "-999999999-01-01", property = "heylogs.from")
    private String from;

    @Parameter(defaultValue = "+999999999-12-31", property = "heylogs.to")
    private String to;

    @Parameter(defaultValue = "0x7fffffff", property = "heylogs.limit")
    private int limit;

    @Parameter(defaultValue = "false", property = "heylogs.skip")
    private boolean skip;

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

        try {
            getLog().info("Reading " + inputFile);
            Document changelog = read();

            VersionFilter filter = getFilter();

            getLog().info("Extracting with " + filter);
            filter.apply(changelog);

            getLog().info("Writing " + outputFile);
            write(changelog);
        } catch (IOException ex) {
            throw new MojoExecutionException("Error while extracting changelog", ex);
        }
    }

    private VersionFilter getFilter() {
        return VersionFilter
                .builder()
                .ref(ref != null ? ref : "")
                .from(VersionFilter.parseLocalDate(from))
                .to(VersionFilter.parseLocalDate(to))
                .limit(limit)
                .build();
    }

    public Document read() throws IOException {
        Parser parser = Parser.builder().build();
        try (Reader reader = Files.newBufferedReader(inputFile.toPath())) {
            return parser.parseReader(reader);
        }
    }

    public void write(Document document) throws IOException {
        Files.createDirectories(outputFile.getParentFile().toPath());
        Formatter formatter = Formatter.builder().build();
        try (Writer writer = Files.newBufferedWriter(outputFile.toPath())) {
            formatter.render(document, writer);
        }
    }
}
