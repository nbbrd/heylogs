package nbbrd.heylogs.cli;

import internal.heylogs.cli.*;
import nbbrd.console.picocli.FileOutputOptions;
import nbbrd.console.picocli.text.TextOutputSupport;
import nbbrd.heylogs.Check;
import nbbrd.heylogs.Heylogs;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static internal.heylogs.FormatSupport.resolveFormatId;
import static internal.heylogs.cli.MarkdownInputSupport.newMarkdownInputSupport;
import static nbbrd.console.picocli.text.TextOutputSupport.newTextOutputSupport;

@Command(name = "check", description = "Check format.")
public final class CheckCommand implements Callable<Integer> {

    @CommandLine.Mixin
    private MultiChangelogInputOptions input;

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
        Heylogs heylogs = heylogsOptions.initHeylogs();

        MarkdownInputSupport inputSupport = newMarkdownInputSupport();

        List<Check> list = new ArrayList<>();
        for (Path file : input.getAllFiles(inputSupport::accept)) {
            list.add(Check
                    .builder()
                    .source(inputSupport.getName(file))
                    .problems(heylogs.checkFormat(inputSupport.readDocument(file)))
                    .build());
        }

        TextOutputSupport outputSupport = newTextOutputSupport();
        Path outputFile = output.getFile();
        String formatId = resolveFormatId(formatOptions.getFormatId(), heylogs, outputSupport::isStdoutFile, outputFile);

        try (Writer writer = outputSupport.newBufferedWriter(outputFile)) {
            heylogs.formatProblems(formatId, writer, list);
        }

        return list.stream().anyMatch(Check::hasErrors) ? CommandLine.ExitCode.SOFTWARE : CommandLine.ExitCode.OK;
    }
}
