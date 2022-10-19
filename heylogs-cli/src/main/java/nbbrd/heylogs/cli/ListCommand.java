package nbbrd.heylogs.cli;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import internal.heylogs.cli.VersionFilterOptions;
import nbbrd.console.picocli.FileInputParameters;
import nbbrd.console.picocli.FileOutputOptions;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.Version;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static internal.heylogs.cli.MarkdownInputSupport.newMarkdownInputSupport;
import static nbbrd.console.picocli.text.TextOutputSupport.newTextOutputSupport;

@Command(name = "list")
public final class ListCommand implements Callable<Void> {

    @CommandLine.Mixin
    private FileInputParameters input;

    @CommandLine.Mixin
    private FileOutputOptions output;

    @CommandLine.Mixin
    private VersionFilterOptions filter;

    @Override
    public Void call() throws Exception {
        store(list(load()));
        return null;
    }

    private Document load() throws IOException {
        return newMarkdownInputSupport().readDocument(input.getFile());
    }

    private List<Heading> list(Node document) {
        return Nodes.of(Heading.class)
                .descendants(document)
                .filter(Version::isVersionLevel)
                .filter(filter.get()::contains)
                .limit(filter.getLimit())
                .collect(Collectors.toList());
    }

    private void store(List<Heading> list) throws IOException {
        try (BufferedWriter writer = newTextOutputSupport().newBufferedWriter(output.getFile())) {
            for (Heading item : list) {
                writer.append(item.getChars());
                writer.newLine();
            }
        }
    }
}
