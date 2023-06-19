package nbbrd.heylogs.cli;

import internal.heylogs.SemverRule;
import internal.heylogs.cli.MarkdownInputSupport;
import nbbrd.console.picocli.FileOutputOptions;
import nbbrd.console.picocli.MultiFileInputOptions;
import nbbrd.heylogs.Checker;
import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.FormatLoader;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.Writer;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.Callable;

import static internal.heylogs.cli.MarkdownInputSupport.newMarkdownInputSupport;
import static nbbrd.console.picocli.text.TextOutputSupport.newTextOutputSupport;

@Command(name = "check", description = "Check changelog format.")
public final class CheckCommand implements Callable<Void> {

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
            names = {"-f", "--formatter"},
            paramLabel = "<name>",
            defaultValue = "stylish",
            description = "Specify the formatter used to control the appearance of the result. Valid values: ${COMPLETION-CANDIDATES}.",
            completionCandidates = FailureFormatters.class
    )
    private String formatId;

    @Override
    public Void call() throws Exception {
        try (Writer writer = newTextOutputSupport().newBufferedWriter(output.getFile())) {

            Checker checker = getChecker();
            MarkdownInputSupport markdown = newMarkdownInputSupport();

            for (Path file : input.getAllFiles(markdown::accept)) {
                checker.formatFailures(
                        formatId,
                        writer,
                        markdown.getName(file),
                        checker.validate(markdown.readDocument(file))
                );
            }
        }

        return null;
    }

    private Checker getChecker() {
        Checker.Builder result = Checker.ofServiceLoader().toBuilder();
        if (semver) {
            result.rule(new SemverRule());
        }
        return result.build();
    }

    public static final class FailureFormatters implements Iterable<String> {

        @Override
        public Iterator<String> iterator() {
            return FormatLoader.load()
                    .stream()
                    .map(Format::getId)
                    .iterator();
        }
    }
}
