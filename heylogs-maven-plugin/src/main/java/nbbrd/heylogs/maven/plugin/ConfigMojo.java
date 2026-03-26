package nbbrd.heylogs.maven.plugin;

import internal.heylogs.maven.plugin.MojoParameterParsing;
import lombok.NonNull;
import nbbrd.heylogs.Config;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import java.nio.file.Path;
import java.util.List;

@lombok.Getter
@lombok.Setter
abstract class ConfigMojo extends HeylogsMojo {

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

    @MojoParameterParsing
    protected @NonNull Config toConfig(Path inputFile) throws MojoExecutionException {
        try {
            // Build mojo parameters config
            Config mojoConfig = Config
                    .builder()
                    .taggingOf(tagging)
                    .versioningOf(versioning)
                    .forgeOf(forge)
                    .rulesOf(rules)
                    .domainsOf(domains)
                    .build();

            if (noConfig) {
                // Ignore config file, only use mojo parameters
                return mojoConfig;
            }

            // Load config from file hierarchy starting from input file's parent directory
            Config fileConfig = Config.loadFromDirectory(Config.resolveStartDir(inputFile));

            // Merge with mojo parameters taking precedence
            return fileConfig.mergeWith(mojoConfig);
        } catch (IllegalArgumentException ex) {
            throw new MojoExecutionException("Invalid config parameter", ex);
        }
    }
}
