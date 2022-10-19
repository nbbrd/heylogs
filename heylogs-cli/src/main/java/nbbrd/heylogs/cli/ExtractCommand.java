package nbbrd.heylogs.cli;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.cli.VersionFilterOptions;
import nbbrd.console.picocli.FileInputParameters;
import nbbrd.console.picocli.FileOutputOptions;
import nbbrd.console.picocli.FileOutputParameters;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.concurrent.Callable;

import static internal.heylogs.cli.MarkdownInputSupport.newMarkdownInputSupport;
import static internal.heylogs.cli.MarkdownOutputSupport.newMarkdownOutputSupport;

@Command(name = "extract")
public final class ExtractCommand implements Callable<Void> {

    @CommandLine.Mixin
    private FileInputParameters input;

    @CommandLine.Mixin
    private FileOutputOptions output;

    @CommandLine.Mixin
    private VersionFilterOptions filter;

    @Override
    public Void call() throws Exception {
        store(extract(load()));
        return null;
    }

    private Document load() throws IOException {
        return newMarkdownInputSupport().readDocument(input.getFile());
    }

    private Document extract(Document document) {
        filter.get().apply(document);
        return document;
    }

    private void store(Document document) throws IOException {
        newMarkdownOutputSupport().writeDocument(output.getFile(), document);
    }
}
