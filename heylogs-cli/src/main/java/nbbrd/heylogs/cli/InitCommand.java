package nbbrd.heylogs.cli;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.cli.ConfigOptions;
import internal.heylogs.cli.SpecialProperties;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.Heylogs;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import static internal.heylogs.HeylogsParameters.DEFAULT_CHANGELOG_FILE;
import static internal.heylogs.cli.MarkdownOutputSupport.newMarkdownOutputSupport;

@Command(name = "init", description = "Initialize a new changelog file.")
public final class InitCommand implements Callable<Void> {

    @CommandLine.Parameters(
            paramLabel = "<file>",
            description = "Output file (default: " + DEFAULT_CHANGELOG_FILE + ").",
            defaultValue = DEFAULT_CHANGELOG_FILE
    )
    private Path file = Paths.get(DEFAULT_CHANGELOG_FILE);

    @CommandLine.Option(
            names = {"--template-file"},
            paramLabel = "<file>",
            description = "Custom Mustache template file."
    )
    private Path templateFile;

    @CommandLine.Option(
            names = {"-p", "--project-url"},
            paramLabel = "<url>",
            description = "Project URL to use for the Unreleased link."
    )
    private URL projectUrl;

    @CommandLine.Mixin
    private ConfigOptions configOptions;

    @CommandLine.Option(
            names = {SpecialProperties.DEBUG_OPTION},
            defaultValue = "false",
            hidden = true
    )
    private boolean debug;

    @Override
    public Void call() throws Exception {
        if (Files.exists(file)) {
            throw new IOException("File already exists: " + file);
        }
        Config config = configOptions.getConfigFromDirectory(Config.resolveStartDir(file));
        String template = templateFile != null ? new String(Files.readAllBytes(templateFile), StandardCharsets.UTF_8) : null;
        Document document = Heylogs.ofServiceLoader().init(config, template, projectUrl);
        newMarkdownOutputSupport().writeDocument(file, document);
        return null;
    }
}
