package nbbrd.heylogs.cli;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.cli.ChangelogInputParameters;
import internal.heylogs.cli.DryRunOptions;
import internal.heylogs.cli.FeedbackSupport;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.Heylogs;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.concurrent.Callable;

import static internal.heylogs.cli.MarkdownInputSupport.newMarkdownInputSupport;
import static internal.heylogs.cli.MarkdownOutputSupport.newMarkdownOutputSupport;

@Command(name = "note", description = "Set the abstract text after the Unreleased header.")
public final class NoteCommand implements Callable<Void> {

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @CommandLine.Mixin
    private ChangelogInputParameters input;

    @CommandLine.Option(
            names = {"-m", "--message"},
            paramLabel = "<message>",
            description = "The summary text.",
            required = true
    )
    private String message;

    @CommandLine.Mixin
    private DryRunOptions dryRunOptions;

    @Override
    public Void call() throws Exception {
        String displayPath = FeedbackSupport.relativize(input.getFile());
        String truncated = message.length() > 40 ? message.substring(0, 40) + "..." : message;
        if (dryRunOptions.isDryRun()) {
            FeedbackSupport.printDryRun(spec, "Would set note \"" + truncated + "\" in " + displayPath);
            return null;
        }
        store(note(load()));
        FeedbackSupport.printSuccess(spec, "Note set \"" + truncated + "\" in " + displayPath);
        return null;
    }

    private Document load() throws IOException {
        return newMarkdownInputSupport().readDocument(input.getFile());
    }

    private Document note(Document document) {
        Heylogs.ofServiceLoader().note(document, message, Config.DEFAULT);
        return document;
    }

    private void store(Document document) throws IOException {
        newMarkdownOutputSupport().writeDocument(input.getFile(), document);
    }
}
