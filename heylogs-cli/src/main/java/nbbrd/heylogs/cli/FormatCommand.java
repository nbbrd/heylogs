package nbbrd.heylogs.cli;

import internal.heylogs.cli.MarkdownInputSupport;
import internal.heylogs.cli.MarkdownOutputSupport;
import internal.heylogs.cli.MultiChangelogInputOptions;
import nbbrd.heylogs.Heylogs;
import com.vladsch.flexmark.util.ast.Document;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import static internal.heylogs.cli.MarkdownInputSupport.newMarkdownInputSupport;
import static internal.heylogs.cli.MarkdownOutputSupport.newMarkdownOutputSupport;

@Command(name = "format", description = "Normalize content and enforce structural ordering.")
public final class FormatCommand implements Callable<Integer> {

    @Mixin
    private MultiChangelogInputOptions input;

    @Option(
            names = {"--check"},
            description = "Check if files are already formatted without modifying them."
    )
    private boolean check;

    @Override
    public Integer call() throws Exception {
        Heylogs heylogs = Heylogs.ofServiceLoader();
        MarkdownInputSupport inputSupport = newMarkdownInputSupport();
        MarkdownOutputSupport outputSupport = newMarkdownOutputSupport();

        boolean unformatted = false;
        for (Path file : input.getAllFiles(inputSupport::accept)) {
            Document document = inputSupport.readDocument(file);
            boolean changed = heylogs.format(document);
            if (check) {
                if (changed) {
                    unformatted = true;
                    System.err.println(inputSupport.getName(file));
                }
            } else {
                outputSupport.writeDocument(file, document);
            }
        }

        return check && unformatted ? CommandLine.ExitCode.SOFTWARE : CommandLine.ExitCode.OK;
    }
}

