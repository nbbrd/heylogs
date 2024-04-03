package nbbrd.heylogs.cli;

import internal.heylogs.StylishFormat;
import internal.heylogs.cli.*;
import nbbrd.console.picocli.FileOutputOptions;
import nbbrd.console.picocli.MultiFileInputOptions;
import nbbrd.heylogs.Heylogs;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.Writer;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import static internal.heylogs.cli.MarkdownInputSupport.newMarkdownInputSupport;
import static nbbrd.console.picocli.text.TextOutputSupport.newTextOutputSupport;

@Command(name = "scan", description = "Summarize changelog content.")
public final class ScanCommand implements Callable<Void> {

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
    public Void call() throws Exception {
        try (Writer writer = newTextOutputSupport().newBufferedWriter(output.getFile())) {

            Heylogs heylogs = heylogsOptions.initHeylogs();
            MarkdownInputSupport markdown = newMarkdownInputSupport();

            for (Path file : input.getAllFiles(markdown::accept)) {
                heylogs.formatStatus(formatOptions.getFormatId(),
                        writer,
                        markdown.getName(file),
                        heylogs.scan(markdown.readDocument(file)));
            }
        }

        return null;
    }
}
