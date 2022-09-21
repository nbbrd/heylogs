package nbbrd.heylogs.cli;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Node;
import internal.heylogs.cli.MarkdownInputOptions;
import internal.heylogs.cli.VersionFilterOptions;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.Version;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "list")
public final class ListCommand implements Callable<Void> {

    @CommandLine.Mixin
    private MarkdownInputOptions input;

    @CommandLine.Mixin
    private VersionFilterOptions filter;

    @Override
    public Void call() throws Exception {
        Node document = input.read();
        list(document);
        return null;
    }

    private void list(Node document) {
        Nodes.of(Heading.class)
                .descendants(document)
                .filter(Version::isVersionLevel)
                .filter(filter.get()::contains)
                .limit(filter.getLimit())
                .map(Node::getChars)
                .forEach(System.out::println);
    }
}
