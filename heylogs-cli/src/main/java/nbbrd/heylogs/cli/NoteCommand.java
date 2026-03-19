package nbbrd.heylogs.cli;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.cli.ChangelogInputParameters;
import internal.heylogs.cli.SpecialProperties;
import nbbrd.heylogs.Heylogs;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.concurrent.Callable;

import static internal.heylogs.cli.MarkdownInputSupport.newMarkdownInputSupport;
import static internal.heylogs.cli.MarkdownOutputSupport.newMarkdownOutputSupport;

@Command(name = "note", description = "Set the abstract text after the Unreleased header.")
public final class NoteCommand implements Callable<Void> {

    @CommandLine.Mixin
    private ChangelogInputParameters input;

    @CommandLine.Option(
            names = {"-m", "--message"},
            paramLabel = "<message>",
            description = "The summary text.",
            required = true
    )
    private String message;

    @CommandLine.Option(
            names = {SpecialProperties.DEBUG_OPTION},
            defaultValue = "false",
            hidden = true
    )
    private boolean debug;

    @Override
    public Void call() throws Exception {
        store(note(load()));
        return null;
    }

    private Document load() throws IOException {
        return newMarkdownInputSupport().readDocument(input.getFile());
    }

    private Document note(Document document) {
        Heylogs.ofServiceLoader().note(document, message);
        return document;
    }

    private void store(Document document) throws IOException {
        newMarkdownOutputSupport().writeDocument(input.getFile(), document);
    }
}

