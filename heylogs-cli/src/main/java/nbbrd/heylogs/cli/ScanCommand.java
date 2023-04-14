package nbbrd.heylogs.cli;

import internal.heylogs.cli.MarkdownInputSupport;
import nbbrd.console.picocli.FileOutputOptions;
import nbbrd.console.picocli.MultiFileInputOptions;
import nbbrd.heylogs.Scan;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.Callable;

import static internal.heylogs.cli.MarkdownInputSupport.newMarkdownInputSupport;
import static nbbrd.console.picocli.text.TextOutputSupport.newTextOutputSupport;

@Command(name = "scan", description = "Summarize changelog content.")
public final class ScanCommand implements Callable<Void> {

    @CommandLine.Mixin
    private MultiFileInputOptions input;

    @CommandLine.Mixin
    private FileOutputOptions output;

    @Override
    public Void call() throws Exception {
        try (BufferedWriter writer = newTextOutputSupport().newBufferedWriter(output.getFile())) {

            MarkdownInputSupport markdown = newMarkdownInputSupport();

            for (Path file : input.getAllFiles(markdown::accept)) {
                write(
                        writer,
                        markdown.getName(file),
                        Scan.of(markdown.readDocument(file)));
            }
        }

        return null;
    }

    private static void write(BufferedWriter writer, String source, Scan scan) throws IOException {
        writer.write(source);
        writer.newLine();
        if (scan.getReleaseCount() == 0) {
            writer.append("  No release found");
            writer.newLine();
        } else {
            writer.append(String.format(Locale.ROOT, "  Found %d releases", scan.getReleaseCount()));
            writer.newLine();
            writer.append(String.format(Locale.ROOT, "  Ranging from %s to %s", scan.getTimeRange().getFrom(), scan.getTimeRange().getTo()));
            writer.newLine();

            if (scan.isCompatibleWithSemver()) {
                writer.append("  Compatible with Semantic Versioning" + scan.getSemverDetails());
                writer.newLine();
            } else {
                writer.append("  Not compatible with Semantic Versioning");
                writer.newLine();
            }
        }
        writer.append(scan.isHasUnreleasedSection() ? "  Has an unreleased version" : "  Has no unreleased version");
        writer.newLine();
        writer.newLine();
    }
}
