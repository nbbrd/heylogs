package nbbrd.heylogs.cli;

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
        return result.build();
    }
}
