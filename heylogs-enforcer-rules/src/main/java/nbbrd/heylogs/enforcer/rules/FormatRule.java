package nbbrd.heylogs.enforcer.rules;

import nbbrd.console.picocli.MultiFileInputOptions;
import nbbrd.heylogs.Heylogs;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static internal.heylogs.HeylogsParameters.DEFAULT_CHANGELOG_FILE;
import static java.util.stream.Collectors.toList;

/**
 * Maven Enforcer rule that checks a changelog file is properly formatted (canonical ordering).
 */
@Named("formatChangelog")
public final class FormatRule extends HeylogsRule {

    private List<File> inputFiles;

    private boolean recursive;

    private boolean skip;

    @Inject
    private org.apache.maven.project.MavenProject project;

    @Override
    public void execute() throws EnforcerRuleException {
        if (skip) {
            getLog().info("Format check has been skipped.");
            return;
        }

        Heylogs heylogs = Heylogs.ofServiceLoader();

        try {
            MultiFileInputOptions input = new MultiFileInputOptions();
            input.setFiles(resolveInputFiles().stream().map(File::toPath).collect(toList()));
            input.setRecursive(recursive);
            input.setSkipErrors(false);
            for (Path file : input.getAllFiles(FormatRule::accept)) {
                if (Files.exists(file)) {
                    if (heylogs.format(readChangelog(file.toFile()))) {
                        throw new EnforcerRuleException("Changelog is not properly formatted: " + file);
                    }
                }
            }
        } catch (EnforcerRuleException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new EnforcerRuleException("Error while listing files", ex);
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

    @Override
    public String toString() {
        return "FormatChangelogRule{inputFiles=" + inputFiles + "}";
    }
}

