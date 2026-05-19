package nbbrd.heylogs.cli;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.cli.ChangelogInputParameters;
import internal.heylogs.cli.DryRunOptions;
import internal.heylogs.cli.FeedbackSupport;
import internal.heylogs.cli.TypeOfChangeOptions;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.Heylogs;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.Callable;

import static internal.heylogs.cli.MarkdownInputSupport.newMarkdownInputSupport;
import static internal.heylogs.cli.MarkdownOutputSupport.newMarkdownOutputSupport;

@Command(name = "push", description = "Push a change to the Unreleased section.")
public final class PushCommand implements Callable<Void> {

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @CommandLine.Mixin
    private ChangelogInputParameters input;

    @CommandLine.Mixin
    private TypeOfChangeOptions typeOfChangeOptions;

    @CommandLine.Option(
            names = {"-m", "--message"},
            paramLabel = "<message>",
            description = "The change message.",
            required = true
    )
    private String message;

    @CommandLine.Mixin
    private DryRunOptions dryRunOptions;

    @Override
    public Void call() throws Exception {
        String displayPath = FeedbackSupport.relativize(input.getFile());
        String type = typeOfChangeOptions.getTypeOfChange().name().toLowerCase(Locale.ROOT);
        String truncated = message.length() > 40 ? message.substring(0, 40) + "..." : message;
        if (dryRunOptions.isDryRun()) {
            FeedbackSupport.printDryRun(spec, "Would push [" + type + "] \"" + truncated + "\" into " + displayPath);
            return null;
        }
        store(push(load()));
        FeedbackSupport.printSuccess(spec, "[" + type + "] \"" + truncated + "\" pushed into " + displayPath);
        return null;
    }

    private Document load() throws IOException {
        return newMarkdownInputSupport().readDocument(input.getFile());
    }

    private Document push(Document document) {
        Heylogs.ofServiceLoader().push(document, typeOfChangeOptions.getTypeOfChange(), message, Config.DEFAULT);
        return document;
    }

    private void store(Document document) throws IOException {
        newMarkdownOutputSupport().writeDocument(input.getFile(), document);
    }
}
