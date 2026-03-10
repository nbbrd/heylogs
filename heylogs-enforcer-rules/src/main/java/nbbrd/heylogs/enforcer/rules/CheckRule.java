package nbbrd.heylogs.enforcer.rules;

import nbbrd.console.picocli.MultiFileInputOptions;
import nbbrd.console.picocli.text.TextOutputSupport;
import nbbrd.heylogs.Check;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.FormatConfig;
import nbbrd.heylogs.Heylogs;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static internal.heylogs.HeylogsParameters.DEFAULT_CHANGELOG_FILE;
import static internal.heylogs.spi.FormatSupport.resolveFormatId;
import static java.util.stream.Collectors.toList;
import static nbbrd.console.picocli.ByteOutputSupport.DEFAULT_STDOUT_FILE;
import static nbbrd.console.picocli.text.TextOutputSupport.newTextOutputSupport;

/**
 * Maven Enforcer rule that checks a changelog file against the keep-a-changelog format.
 */
@Named("checkChangelog")
public final class CheckRule extends HeylogsRule {

    private List<File> inputFiles;

    private boolean recursive;

    private File outputFile;

    private String tagging;

    private String versioning;

    private String forge;

    private List<String> rules;

    private List<String> domains;

    private String format;

    private boolean skip;

    private boolean noConfig;

    @Inject
    private org.apache.maven.project.MavenProject project;

    @Override
    public void execute() throws EnforcerRuleException {
        if (skip) {
            getLog().info("Checking has been skipped.");
            return;
        }

        Config config = toConfig();
        Heylogs heylogs = Heylogs.ofServiceLoader();

        List<Check> list = new ArrayList<>();
        try {
            MultiFileInputOptions input = new MultiFileInputOptions();
            input.setFiles(resolveInputFiles().stream().map(File::toPath).collect(toList()));
            input.setRecursive(recursive);
            input.setSkipErrors(false);
            for (Path file : input.getAllFiles(CheckRule::accept)) {
                if (Files.exists(file)) {
                    list.add(Check
                            .builder()
                            .source(file.toString())
                            .problems(heylogs.check(readChangelog(file.toFile()), config))
                            .build());
                }
            }
        } catch (IOException ex) {
            throw new EnforcerRuleException("Error while listing files", ex);
        }
        boolean hasErrors = list.stream().anyMatch(Check::hasErrors);

        File resolvedOutputFile = resolveOutputFile();

        TextOutputSupport outputSupport = newTextOutputSupport();
        String formatId = resolveFormatId(format != null ? FormatConfig.parse(format) : null, heylogs, outputSupport::isStdoutFile, resolvedOutputFile.toPath());

        try (Writer writer = newWriter(resolvedOutputFile, hasErrors ? getLog()::error : getLog()::info)) {
            heylogs.formatProblems(formatId, writer, list);
        } catch (IOException ex) {
            throw new EnforcerRuleException("Error while writing", ex);
        }

        if (hasErrors) {
            throw new EnforcerRuleException("Invalid changelog");
        }
    }

    private Config toConfig() throws EnforcerRuleException {
        try {
            // Build enforcer rule parameters config
            Config ruleConfig = Config
                    .builder()
                    .taggingOf(tagging)
                    .versioningOf(versioning)
                    .forgeOf(forge)
                    .rulesOf(rules)
                    .domainsOf(domains)
                    .build();

            if (noConfig) {
                // Ignore config file, only use enforcer rule parameters
                return ruleConfig;
            }

            // Load config from file hierarchy starting from project basedir or first input file
            Path projectBasedir = project != null ? project.getBasedir().toPath() : null;
            Path firstInputFile = (inputFiles != null && !inputFiles.isEmpty()) ? inputFiles.get(0).toPath() : null;
            Config fileConfig = Config.loadFromDirectory(Config.resolveStartDir(projectBasedir, firstInputFile));

            // Merge with enforcer rule parameters taking precedence
            return fileConfig.mergeWith(ruleConfig);
        } catch (IllegalArgumentException ex) {
            throw new EnforcerRuleException("Invalid config parameter", ex);
        }
    }

    private List<File> resolveInputFiles() {
        if (inputFiles != null && !inputFiles.isEmpty()) {
            return inputFiles;
        }
        List<File> result = new ArrayList<>();
        if (project != null) {
            result.add(project.getBasedir().toPath().resolve(DEFAULT_CHANGELOG_FILE).toFile());
        } else {
            result.add(Paths.get(DEFAULT_CHANGELOG_FILE).toFile());
        }
        return result;
    }

    private File resolveOutputFile() {
        return outputFile != null ? outputFile : Paths.get(DEFAULT_STDOUT_FILE).toFile();
    }

    @Override
    public String toString() {
        return "CheckChangelogRule{inputFiles=" + inputFiles + "}";
    }
}

