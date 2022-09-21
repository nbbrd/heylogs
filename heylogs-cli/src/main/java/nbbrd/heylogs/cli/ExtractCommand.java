package nbbrd.heylogs.cli;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.cli.MarkdownInputOptions;
import internal.heylogs.cli.MarkdownOutputOptions;
import internal.heylogs.cli.VersionFilterOptions;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "extract")
public final class ExtractCommand implements Callable<Void> {

    @CommandLine.Mixin
    private MarkdownInputOptions input;

    @CommandLine.Mixin
    private MarkdownOutputOptions output;

    @CommandLine.Mixin
    private VersionFilterOptions filter;

    @Override
    public Void call() throws Exception {
        Document document = input.read();
        filter.get().apply(document);
        output.write(document);
        return null;
    }
}
