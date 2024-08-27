package nbbrd.heylogs.cli;

import internal.heylogs.cli.*;
import nbbrd.console.picocli.FileOutputOptions;
import nbbrd.heylogs.Check;
import nbbrd.heylogs.Heylogs;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static internal.heylogs.cli.MarkdownInputSupport.newMarkdownInputSupport;
import static nbbrd.console.picocli.text.TextOutputSupport.newTextOutputSupport;

@Command(name = "check", description = "Check changelog format.")
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
        try (Writer writer = newTextOutputSupport().newBufferedWriter(output.getFile())) {

            Heylogs heylogs = heylogsOptions.initHeylogs();
            MarkdownInputSupport markdown = newMarkdownInputSupport();

            List<Check> list = new ArrayList<>();
            for (Path file : input.getAllFiles(markdown::accept)) {
                list.add(Check
                        .builder()
                        .source(markdown.getName(file))
                        .problems(heylogs.checkFormat(markdown.readDocument(file)))
                        .build());
            }
            heylogs.formatProblems(formatOptions.getFormatId(), writer, list);
            return list.stream().anyMatch(Check::hasErrors) ? CommandLine.ExitCode.SOFTWARE : CommandLine.ExitCode.OK;
        }
    }
}
