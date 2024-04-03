package nbbrd.heylogs.cli;

import internal.heylogs.cli.FormatOptions;
import internal.heylogs.cli.HeylogsOptions;
import internal.heylogs.cli.MarkdownInputSupport;
import internal.heylogs.cli.SpecialProperties;
import nbbrd.console.picocli.FileOutputOptions;
import nbbrd.console.picocli.MultiFileInputOptions;
import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.Problem;
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

    @CommandLine.Mixin
    private HeylogsOptions heylogsOptions;

    @CommandLine.Mixin
    private FormatOptions formatOptions;

    @CommandLine.Option(
            names = {SpecialProperties.DEBUG_OPTION},
            defaultValue = "false",
            hidden = true
    )
    private boolean debug;

    @Override
    public Integer call() throws Exception {
        try (Writer writer = newTextOutputSupport().newBufferedWriter(output.getFile())) {

            Heylogs heylogs = heylogsOptions.initHeylogs();
            MarkdownInputSupport markdown = newMarkdownInputSupport();

            int returnCode = CommandLine.ExitCode.OK;
            for (Path file : input.getAllFiles(markdown::accept)) {
                List<Problem> problems = heylogs.validate(markdown.readDocument(file));
                heylogs.formatProblems(formatOptions.getFormatId(), writer, markdown.getName(file), problems);
                if (returnCode == CommandLine.ExitCode.OK && Problem.hasErrors(problems)) {
                    returnCode = CommandLine.ExitCode.SOFTWARE;
                }
            }
            return returnCode;
        }
    }
}
