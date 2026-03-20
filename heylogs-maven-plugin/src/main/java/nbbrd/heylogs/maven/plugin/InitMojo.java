package nbbrd.heylogs.maven.plugin;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.maven.plugin.MojoParameterParsing;
import lombok.NonNull;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.Heylogs;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static internal.heylogs.HeylogsParameters.DEFAULT_CHANGELOG_FILE;

@lombok.Getter
@lombok.Setter
@Mojo(name = "init", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true, requiresProject = false)
public final class InitMojo extends HeylogsMojo {

    @Parameter(property = "heylogs.outputFile", defaultValue = DEFAULT_CHANGELOG_FILE)
    private File outputFile;

    @Parameter(property = "heylogs.templateFile")
    private File templateFile;

    @Parameter(property = "heylogs.projectUrl")
    private URL projectUrl;

    @Parameter(property = "heylogs.tagging")
    private String tagging;

    @Parameter(property = "heylogs.versioning")
    private String versioning;

    @Parameter(property = "heylogs.forge")
    private String forge;

    @Parameter(property = "heylogs.rules")
    private List<String> rules;

    @Parameter(property = "heylogs.domains")
    private List<String> domains;

    @Parameter(property = "heylogs.noConfig", defaultValue = "false")
    private boolean noConfig;

    @Override
    public void execute() throws MojoExecutionException {
        if (isSkip()) {
            getLog().info("Init has been skipped.");
            return;
        }

        if (outputFile.exists()) {
            getLog().info("Changelog already exists: " + outputFile);
            return;
        }

        Config config = toConfig();

        getLog().info("Initializing changelog " + outputFile);
        try {
            String template = templateFile != null ? new String(Files.readAllBytes(templateFile.toPath()), StandardCharsets.UTF_8) : null;
            Document document = Heylogs.ofServiceLoader().init(config, template, projectUrl);
            writeChangelog(document, outputFile);
        } catch (IOException ex) {
            throw new MojoExecutionException("Failed to read template file", ex);
        }
    }

    @MojoParameterParsing
    private @NonNull Config toConfig() throws MojoExecutionException {
        try {
            Config mojoConfig = Config
                    .builder()
                    .taggingOf(tagging)
                    .versioningOf(versioning)
                    .forgeOf(forge)
                    .rulesOf(rules)
                    .domainsOf(domains)
                    .build();

            if (noConfig) {
                return mojoConfig;
            }

            Config fileConfig = Config.loadFromDirectory(Config.resolveStartDir(outputFile.toPath()));
            return fileConfig.mergeWith(mojoConfig);
        } catch (IllegalArgumentException ex) {
            throw new MojoExecutionException("Invalid config parameter", ex);
        }
    }
}
