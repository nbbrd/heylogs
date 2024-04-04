package nbbrd.heylogs.cli;

import internal.heylogs.cli.FormatOptions;
import internal.heylogs.cli.HeylogsOptions;
import internal.heylogs.cli.MarkdownInputSupport;
import internal.heylogs.cli.SpecialProperties;
import nbbrd.console.picocli.FileOutputOptions;
import nbbrd.console.picocli.MultiFileInputOptions;
import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.Scan;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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

            List<Scan> list = new ArrayList<>();
            for (Path file : input.getAllFiles(markdown::accept)) {
                list.add(Scan
                        .builder()
                        .source(markdown.getName(file))
                        .summary(heylogs.scan(markdown.readDocument(file)))
                        .build());
            }
            heylogs.formatStatus(formatOptions.getFormatId(), writer, list);
        }

        return null;
    }
}
