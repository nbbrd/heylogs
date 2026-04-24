package nbbrd.heylogs.cli;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.cli.*;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.Heylogs;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.concurrent.Callable;

import static internal.heylogs.cli.MarkdownInputSupport.newMarkdownInputSupport;
import static internal.heylogs.cli.MarkdownOutputSupport.newMarkdownOutputSupport;

@Command(name = "fetch", description = "Fetch a change from a forge into the Unreleased section.")
public final class FetchCommand implements Callable<Void> {

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

    @Override
    public Void call() throws Exception {
        Config config = configOptions.getConfigFromDirectory(Config.resolveStartDir(input.getFile()));
        store(fetch(load(), config));
        return null;
    }

    private Document load() throws IOException {
        return newMarkdownInputSupport().readDocument(input.getFile());
    }

    private Document fetch(Document document, Config config) throws IOException {
        Heylogs.ofServiceLoader().fetch(document, typeOfChangeOptions.getTypeOfChange(), issue, config);
        return document;
    }

    private void store(Document document) throws IOException {
        newMarkdownOutputSupport().writeDocument(input.getFile(), document);
    }
}
