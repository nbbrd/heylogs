package nbbrd.heylogs.cli;

import internal.heylogs.cli.FormatOptions;
import internal.heylogs.cli.MarkdownInputSupport;
import internal.heylogs.cli.MultiChangelogInputOptions;
import internal.heylogs.cli.SpecialProperties;
import nbbrd.console.picocli.FileOutputOptions;
import nbbrd.console.picocli.text.TextOutputSupport;
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
import static internal.heylogs.spi.FormatSupport.resolveFormatId;
import static nbbrd.console.picocli.text.TextOutputSupport.newTextOutputSupport;

@Command(name = "scan", description = "Summarize content.")
public final class ScanCommand implements Callable<Void> {

    @CommandLine.Mixin
    private MultiChangelogInputOptions input;

    @CommandLine.Mixin
    private FileOutputOptions output;

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
        Heylogs heylogs = Heylogs.ofServiceLoader();

        MarkdownInputSupport inputSupport = newMarkdownInputSupport();

        List<Scan> list = new ArrayList<>();
        for (Path file : input.getAllFiles(inputSupport::accept)) {
            list.add(Scan
                    .builder()
                    .source(inputSupport.getName(file))
                    .summary(heylogs.scan(inputSupport.readDocument(file)))
                    .build());
        }

        TextOutputSupport outputSupport = newTextOutputSupport();
        Path outputFile = output.getFile();
        String formatId = resolveFormatId(formatOptions.getFormat(), heylogs, outputSupport::isStdoutFile, outputFile);

        try (Writer writer = outputSupport.newBufferedWriter(outputFile)) {
            heylogs.formatStatus(formatId, writer, list);
        }

        return null;
    }
}
