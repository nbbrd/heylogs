package nbbrd.heylogs.cli;

import internal.heylogs.StylishFormat;
import internal.heylogs.cli.FormatCandidates;
import internal.heylogs.cli.MarkdownInputSupport;
import nbbrd.console.picocli.FileOutputOptions;
import nbbrd.console.picocli.MultiFileInputOptions;
import nbbrd.heylogs.Scanner;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.BufferedWriter;
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

    @CommandLine.Option(
            names = {"-f", "--format"},
            paramLabel = "<name>",
            defaultValue = StylishFormat.ID,
            description = "Specify the format used to control the appearance of the result. Valid values: ${COMPLETION-CANDIDATES}.",
            completionCandidates = FormatCandidates.class
    )
    private String formatId;

    @Override
    public Void call() throws Exception {
        try (BufferedWriter writer = newTextOutputSupport().newBufferedWriter(output.getFile())) {

            Scanner scanner = getScanner();
            MarkdownInputSupport markdown = newMarkdownInputSupport();

            for (Path file : input.getAllFiles(markdown::accept)) {
                scanner.formatStatus(
                        writer,
                        markdown.getName(file),
                        scanner.scan(markdown.readDocument(file)));
            }
        }

        return null;
    }

    private Scanner getScanner() {
        return Scanner.ofServiceLoader()
                .toBuilder()
                .formatId(formatId)
                .build();
    }
}
