package nbbrd.heylogs.cli;

import internal.heylogs.cli.MarkdownInputOptions;
import nbbrd.console.picocli.text.TextOutputOptions;
import nbbrd.heylogs.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "check")
public final class CheckCommand implements Callable<Integer> {

    @CommandLine.Mixin
    private MarkdownInputOptions input;

    @CommandLine.Mixin
    private TextOutputOptions output;

    @CommandLine.Option(
            names = {"-f", "--format"},
            defaultValue = "stylish",
            description = "Specify the formatter used to control the appearance of the result. Valid values: ${COMPLETION-CANDIDATES}.",
            completionCandidates = FormatCandidates.class
    )
    private String format;

    @CommandLine.Option(
            names = {"--semver"},
            defaultValue = "false",
            description = "Mention if this changelog follows Semantic Versioning."
    )
    private boolean semver;

    @Override
    public Integer call() throws Exception {
        if (semver) {
            System.setProperty(Rule.ENABLE_KEY, "semver");
        }

        List<Failure> failures = Failure.allOf(input.read(), RuleLoader.load());

        try (Writer writer = output.newCharWriter()) {
            FailureFormatterLoader.load()
                    .stream()
                    .filter(formatter -> formatter.getName().equals(format))
                    .findFirst()
                    .orElse(new StylishFormatter())
                    .format(writer, input.hasFile() ? input.getFile().toString() : "stdin", failures);
        }

        return failures.isEmpty() ? CommandLine.ExitCode.OK : CommandLine.ExitCode.USAGE;
    }

    public static final class FormatCandidates implements Iterable<String> {

        @Override
        public Iterator<String> iterator() {
            return FailureFormatterLoader.load()
                    .stream()
                    .map(FailureFormatter::getName)
                    .iterator();
        }
    }
}
