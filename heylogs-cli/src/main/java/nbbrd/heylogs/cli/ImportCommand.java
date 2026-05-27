package nbbrd.heylogs.cli;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.cli.ChangelogOutputParameters;
import internal.heylogs.cli.DryRunOptions;
import internal.heylogs.cli.FeedbackSupport;
import internal.heylogs.cli.FormatOptions;
import nbbrd.heylogs.ChangelogContent;
import nbbrd.heylogs.Heylogs;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import static internal.heylogs.cli.MarkdownOutputSupport.newMarkdownOutputSupport;
import static nbbrd.heylogs.spi.FormatSupport.resolveFormatId;
import nbbrd.heylogs.spi.FormatType;

@Command(name = "import", description = "Import changelog content from structured data.")
public final class ImportCommand implements Callable<Void> {

    @CommandLine.Parameters(
            paramLabel = "<source>",
            description = "Input file."
    )
    private Path inputFile;

    @CommandLine.Mixin
    private ChangelogOutputParameters output;

    @CommandLine.Mixin
    private FormatOptions formatOptions;

    @CommandLine.Mixin
    private DryRunOptions dryRunOptions;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Void call() throws Exception {
        Heylogs heylogs = Heylogs.ofServiceLoader();

        String formatId = resolveFormatId(formatOptions.getFormat(), heylogs, path -> false, inputFile, FormatType.CONTENT);

        if (dryRunOptions.isDryRun()) {
            FeedbackSupport.printDryRun(spec, "Would import " + inputFile + " -> " + output.getFile());
            return null;
        }

        ChangelogContent content;
        try (Reader reader = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8)) {
            content = heylogs.parseContent(formatId, reader);
        }

        Document document = content.toDocument();
        newMarkdownOutputSupport().writeDocument(output.getFile(), document);
        FeedbackSupport.printSuccess(spec, "Imported " + inputFile + " -> " + output.getFile());

        return null;
    }
}
