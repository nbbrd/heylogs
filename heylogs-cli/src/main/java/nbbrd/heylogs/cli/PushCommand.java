package nbbrd.heylogs.cli;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.cli.ChangelogInputParameters;
import internal.heylogs.cli.SpecialProperties;
import internal.heylogs.cli.TypeOfChangeCandidates;
import internal.heylogs.cli.TypeOfChangeConverter;
import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.TypeOfChange;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.concurrent.Callable;

import static internal.heylogs.cli.MarkdownInputSupport.newMarkdownInputSupport;
import static internal.heylogs.cli.MarkdownOutputSupport.newMarkdownOutputSupport;

@Command(name = "push", description = "Push a change to the Unreleased section.")
public final class PushCommand implements Callable<Void> {

    @CommandLine.Mixin
    private ChangelogInputParameters input;

    @CommandLine.Option(
            names = {"-t", "--type"},
            paramLabel = "<type>",
            description = "Type of change. Valid values: ${COMPLETION-CANDIDATES}.",
            required = true,
            completionCandidates = TypeOfChangeCandidates.class,
            converter = TypeOfChangeConverter.class
    )
    private TypeOfChange typeOfChange;

    @CommandLine.Option(
            names = {"-m", "--message"},
            paramLabel = "<message>",
            description = "The change message.",
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
        store(push(load()));
        return null;
    }

    private Document load() throws IOException {
        return newMarkdownInputSupport().readDocument(input.getFile());
    }

    private Document push(Document document) {
        Heylogs.ofServiceLoader().push(document, typeOfChange, message);
        return document;
    }

    private void store(Document document) throws IOException {
        newMarkdownOutputSupport().writeDocument(input.getFile(), document);
    }
}


