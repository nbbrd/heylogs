package nbbrd.heylogs.cli;

import internal.heylogs.cli.DryRunOptions;
import internal.heylogs.cli.FeedbackSupport;
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
import java.util.List;
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

    @Mixin
    private DryRunOptions dryRunOptions;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        Heylogs heylogs = Heylogs.ofServiceLoader();
        MarkdownInputSupport inputSupport = newMarkdownInputSupport();
        MarkdownOutputSupport outputSupport = newMarkdownOutputSupport();

        List<Path> allFiles = input.getAllFiles(inputSupport::accept);
        boolean multiFile = allFiles.size() > 1;

        int formattedCount = 0;
        int upToDateCount = 0;
        int unformattedCount = 0;

        for (Path file : allFiles) {
            String displayPath = inputSupport.getName(file);
            Document document = inputSupport.readDocument(file);
            boolean changed = heylogs.format(document);
            if (dryRunOptions.isDryRun()) {
                if (changed) {
                    formattedCount++;
                    FeedbackSupport.printDryRun(spec, "Would format: " + displayPath);
                } else {
                    upToDateCount++;
                    FeedbackSupport.printNoOp(spec, "Already formatted: " + displayPath);
                }
            } else if (check) {
                if (changed) {
                    unformattedCount++;
                    FeedbackSupport.printWarning(spec, "Not formatted: " + displayPath);
                } else {
                    FeedbackSupport.printSuccess(spec, "Properly formatted: " + displayPath);
                }
            } else {
                if (changed) {
                    formattedCount++;
                    outputSupport.writeDocument(file, document);
                    FeedbackSupport.printSuccess(spec, "Formatted: " + displayPath);
                } else {
                    upToDateCount++;
                    FeedbackSupport.printNoOp(spec, "Already formatted: " + displayPath);
                }
            }
        }

        if (multiFile) {
            if (dryRunOptions.isDryRun()) {
                FeedbackSupport.printDryRun(spec, formattedCount + " would be formatted, " + upToDateCount + " already up-to-date");
            } else if (check) {
                if (unformattedCount == 0) {
                    FeedbackSupport.printSuccess(spec, "All " + allFiles.size() + " file(s) properly formatted");
                } else {
                    FeedbackSupport.printWarning(spec, unformattedCount + " of " + allFiles.size() + " file(s) need formatting");
                }
            } else {
                FeedbackSupport.printSuccess(spec, formattedCount + " formatted, " + upToDateCount + " already up-to-date");
            }
        }

        return !dryRunOptions.isDryRun() && check && unformattedCount > 0 ? CommandLine.ExitCode.SOFTWARE : CommandLine.ExitCode.OK;
    }
}
