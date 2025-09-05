package nbbrd.heylogs.cli;

import internal.heylogs.cli.FormatOptions;
import internal.heylogs.cli.SpecialProperties;
import nbbrd.console.picocli.FileOutputOptions;
import nbbrd.console.picocli.text.TextOutputSupport;
import nbbrd.heylogs.Heylogs;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import static internal.heylogs.spi.FormatSupport.resolveFormatId;
import static nbbrd.console.picocli.text.TextOutputSupport.newTextOutputSupport;

@Command(name = "list", description = "List resources.")
public final class ListCommand implements Callable<Void> {

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
    public Void call() throws IOException {
        Heylogs heylogs = Heylogs.ofServiceLoader();

        TextOutputSupport outputSupport = newTextOutputSupport();
        Path outputFile = output.getFile();
        String formatId = resolveFormatId(formatOptions.getFormat(), heylogs, outputSupport::isStdoutFile, outputFile);

        try (Writer writer = outputSupport.newBufferedWriter(outputFile)) {
            heylogs.formatResources(formatId, writer, heylogs.list());
        }

        return null;
    }
}
