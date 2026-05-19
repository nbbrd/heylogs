package nbbrd.heylogs.cli;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.cli.*;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.Heylogs;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.Callable;

import static internal.heylogs.cli.MarkdownInputSupport.newMarkdownInputSupport;
import static internal.heylogs.cli.MarkdownOutputSupport.newMarkdownOutputSupport;

@Command(name = "fetch", description = "Fetch a change from a forge into the Unreleased section.")
public final class FetchCommand implements Callable<Void> {

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @CommandLine.Mixin
    private ChangelogInputParameters input;

    @CommandLine.Mixin
    private TypeOfChangeOptions typeOfChangeOptions;

    @CommandLine.Option(
            names = {"-i", "--id"},
            paramLabel = "<id>",
            description = "URL or ref of the forge issue or pull request.",
            required = true
    )
    private String issue;

    @CommandLine.Mixin
    private ConfigOptions configOptions;

    @CommandLine.Mixin
    private DryRunOptions dryRunOptions;

    @Override
    public Void call() throws Exception {
        String displayPath = FeedbackSupport.relativize(input.getFile());
        String type = typeOfChangeOptions.getTypeOfChange().name().toLowerCase(Locale.ROOT);
        if (dryRunOptions.isDryRun()) {
            FeedbackSupport.printDryRun(spec, "Would fetch [" + type + "] " + issue + " into " + displayPath);
            return null;
        }
        Config config = configOptions.getConfigFromDirectory(Config.resolveStartDir(input.getFile()));
        Document document = load();
        String snapshot = FeedbackSupport.renderDocument(document);
        long startNano = System.nanoTime();
        fetch(document, config);
        String elapsed = FeedbackSupport.formatElapsed(startNano);
        if (!snapshot.equals(FeedbackSupport.renderDocument(document))) {
            store(document);
            FeedbackSupport.printSuccess(spec, "[" + type + "] " + issue + " fetched into " + displayPath
                    + " " + FeedbackSupport.faint(spec, "(" + elapsed + ")"));
        } else {
            FeedbackSupport.printNoOp(spec, "Already present: " + issue + " in " + displayPath);
        }
        return null;
    }

    private Document load() throws IOException {
        return newMarkdownInputSupport().readDocument(input.getFile());
    }

    private void fetch(Document document, Config config) throws IOException {
        Heylogs.ofServiceLoader().fetch(document, typeOfChangeOptions.getTypeOfChange(), issue, config);
    }

    private void store(Document document) throws IOException {
        newMarkdownOutputSupport().writeDocument(input.getFile(), document);
    }
}
