package nbbrd.heylogs.cli;

import internal.heylogs.cli.FailureFormatOptions;
import internal.heylogs.cli.MarkdownInputSupport;
import internal.heylogs.cli.RuleSetOptions;
import nbbrd.console.picocli.FileOutputOptions;
import nbbrd.console.picocli.MultiFileInputOptions;
import nbbrd.heylogs.Failure;
import nbbrd.heylogs.FailureFormatter;
import nbbrd.heylogs.Rule;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import static internal.heylogs.cli.MarkdownInputSupport.newMarkdownInputSupport;
import static nbbrd.console.picocli.text.TextOutputSupport.newTextOutputSupport;

@Command(name = "check", description = "Check changelog format.")
public final class CheckCommand implements Callable<Void> {

    @CommandLine.Mixin
    private MultiFileInputOptions input;

    @CommandLine.Mixin
    private FileOutputOptions output;

    @CommandLine.Mixin
    private RuleSetOptions ruleSet;

    @CommandLine.Mixin
    private FailureFormatOptions format;

    @Override
    public Void call() throws Exception {
        try (Writer writer = newTextOutputSupport().newBufferedWriter(output.getFile())) {

            List<Rule> rules = ruleSet.getRules();
            FailureFormatter formatter = format.getFormatter();
            MarkdownInputSupport markdown = newMarkdownInputSupport();

            for (Path file : input.getAllFiles(markdown::accept)) {
                formatter.format(
                        writer,
                        markdown.getName(file),
                        Failure.allOf(markdown.readDocument(file), rules)
                );
            }
        }

        return null;
    }
}
