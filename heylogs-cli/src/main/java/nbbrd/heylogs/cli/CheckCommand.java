package nbbrd.heylogs.cli;

import com.vladsch.flexmark.util.misc.Pair;
import internal.heylogs.GitDiffRule;
import internal.heylogs.SemverRule;
import internal.heylogs.StylishFormat;
import internal.heylogs.cli.FormatCandidates;
import internal.heylogs.cli.MarkdownInputSupport;
import internal.heylogs.cli.SpecialProperties;
import nbbrd.console.picocli.FileOutputOptions;
import nbbrd.console.picocli.MultiFileInputOptions;
import nbbrd.heylogs.Checker;
import nbbrd.heylogs.Failure;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import static internal.heylogs.cli.MarkdownInputSupport.newMarkdownInputSupport;
import static nbbrd.console.picocli.text.TextOutputSupport.newTextOutputSupport;

@Command(name = "check", description = "Check changelog format.")
public final class CheckCommand implements Callable<Integer> {

    @CommandLine.Mixin
    private MultiFileInputOptions input;

    @CommandLine.Mixin
    private FileOutputOptions output;

    @CommandLine.Option(
            names = {"-s", "--semver"},
            defaultValue = "false",
            description = "Mention if this changelog follows Semantic Versioning."
    )
    private boolean semver;

    @CommandLine.Option(
            names = {"-f", "--format"},
            paramLabel = "<name>",
            defaultValue = StylishFormat.ID,
            description = "Specify the format used to control the appearance of the result. Valid values: ${COMPLETION-CANDIDATES}.",
            completionCandidates = FormatCandidates.class
    )
    private String formatId;

    @CommandLine.Option(
            names = {"-g", "--gitdiff"},
            description = "Checks the git diff whether the contents of a released version are changed. A git revision range might be main...patch-1. Default is to compare the HEAD commit with its first parent.",
            arity = "0..1",
            defaultValue = "-1...-1",
            fallbackValue = "1...1",
            paramLabel = "<git revision range>",
            converter = GitRangeConverter.class
    )
    private Pair<String, String> checkGit;

    // variable needed to "inject" directory of checked file
    private GitDiffRule gitDiffRule;

    /**
     *  Wrapper to interpret shortcuts. In case user did not specify -g, the "defaultValue" "-1...-1" is set (and thus, heylogs knows that gitDiff is disabled.
     */
    private boolean isGitDiffEnabled() {
        return !checkGit.equals(new Pair("-1", "-1"));
    }

    /**
     * @return In case "-g" is provided without any more arguments, this method returns "true"
     */
    private boolean useHeadAndParent() {
        return checkGit.equals(new Pair("1", "1"));
    }

    @CommandLine.Option(
            names = {SpecialProperties.DEBUG_OPTION},
            defaultValue = "false",
            hidden = true
    )
    private boolean debug;

    @Override
    public Integer call() throws Exception {
        try (Writer writer = newTextOutputSupport().newBufferedWriter(output.getFile())) {

            Checker checker = getChecker();
            MarkdownInputSupport markdown = newMarkdownInputSupport();

            int failureCount = 0;
            for (Path file : input.getAllFiles(markdown::accept)) {
                if (isGitDiffEnabled()) {
                    gitDiffRule.setPath(file.getParent());
                }
                List<Failure> failures = checker.validate(markdown.readDocument(file));
                checker.formatFailures(writer, markdown.getName(file), failures);
                failureCount += failures.size();
            }
            return failureCount;
        }
    }

    private Checker getChecker() {
        Checker.Builder result = Checker.ofServiceLoader()
                .toBuilder()
                .formatId(formatId);
        if (semver) {
            result.rule(new SemverRule());
        }
        if (isGitDiffEnabled()) {
            if (useHeadAndParent()) {
                gitDiffRule = new GitDiffRule();
            } else {
                gitDiffRule = new GitDiffRule(checkGit.getFirst(), checkGit.getSecond());
            }
            result.rule(gitDiffRule);
        }
        return result.build();
    }
}
