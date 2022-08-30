package nbbrd.heylogs.cli;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.RefNode;
import com.vladsch.flexmark.ast.Reference;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import internal.heylogs.cli.MarkdownInputOptions;
import internal.heylogs.cli.MarkdownOutputOptions;
import internal.heylogs.cli.VersionFilter;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.Version;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "extract")
public final class ExtractCommand implements Callable<Void> {

    @CommandLine.Mixin
    private MarkdownInputOptions input;

    @CommandLine.Mixin
    private MarkdownOutputOptions output;

    @CommandLine.Mixin
    private VersionFilter filter;

    @Override
    public Void call() throws Exception {
        Document document = input.read();
        extract(document);
        output.write(document);
        return null;
    }

    private void extract(Document root) {
        int found = 0;
        boolean keep = false;

        List<String> refNodes = new ArrayList<>();
        List<Reference> references = new ArrayList<>();

        for (Node current : root.getChildren()) {

            if (current instanceof Heading && Version.isVersionLevel((Heading) current)) {
                if (found >= filter.getLimit() || !filter.contains((Heading) current)) {
                    keep = false;
                } else {
                    found++;
                    keep = true;
                }
            }

            if (keep) {
                Nodes.of(RefNode.class)
                        .descendants(current)
                        .map(node -> node.getReference().toString())
                        .forEach(refNodes::add);
            } else {
                if (current instanceof Reference) {
                    references.add((Reference) current);
                } else {
                    current.unlink();
                }
            }
        }

        references
                .stream()
                .filter(reference -> !refNodes.contains(reference.getReference().toString()))
                .forEach(Node::unlink);
    }
}
