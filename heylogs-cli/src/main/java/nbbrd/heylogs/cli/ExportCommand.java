package nbbrd.heylogs.cli;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.cli.ChangelogInputParameters;
import internal.heylogs.cli.FormatOptions;
import nbbrd.console.picocli.FileOutputOptions;
import nbbrd.console.picocli.text.TextOutputSupport;
import nbbrd.heylogs.ChangelogContent;
import nbbrd.heylogs.Heylogs;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import static internal.heylogs.cli.MarkdownInputSupport.newMarkdownInputSupport;
import static nbbrd.console.picocli.text.TextOutputSupport.newTextOutputSupport;
import static nbbrd.heylogs.spi.FormatSupport.resolveFormatId;
import nbbrd.heylogs.spi.FormatType;

@Command(name = "export", description = "Export changelog content as structured data.")
public final class ExportCommand implements Callable<Void> {

    @CommandLine.Mixin
    private ChangelogInputParameters input;

    @CommandLine.Mixin
    private FileOutputOptions output;

    @CommandLine.Mixin
    private FormatOptions formatOptions;

    @Override
    public Void call() throws Exception {
        Heylogs heylogs = Heylogs.ofServiceLoader();

        Document document = newMarkdownInputSupport().readDocument(input.getFile());

        ChangelogContent content = heylogs.content(document)
                .orElseThrow(() -> new IOException("No changelog found in " + input.getFile()));

        TextOutputSupport outputSupport = newTextOutputSupport();
        Path outputFile = output.getFile();
        String formatId = resolveFormatId(formatOptions.getFormat(), heylogs, outputSupport::isStdoutFile, outputFile, FormatType.CONTENT);

        try (Writer writer = outputSupport.newBufferedWriter(outputFile)) {
            heylogs.formatContent(formatId, writer, content);
        }

        return null;
    }
}
