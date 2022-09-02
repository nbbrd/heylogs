package nbbrd.heylogs.cli;

import com.vladsch.flexmark.util.ast.Node;
import internal.heylogs.cli.MarkdownInputOptions;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.ExtendedRules;
import nbbrd.heylogs.GuidingPrinciples;
import nbbrd.heylogs.Rule;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Command(name = "check")
public final class CheckCommand implements Callable<Integer> {

    @CommandLine.Mixin
    private MarkdownInputOptions input;

    @Override
    public Integer call() throws Exception {
        Node document = input.read();
        return check(document);
    }

    private Integer check(Node document) {
        List<Rule<Node>> rules = getRules();
        System.out.println("Using rules: " + rules);

        Function<Node, Stream<String>> visitor =
                node -> rules.stream()
                        .map(rule -> rule.validate(node))
                        .filter(Objects::nonNull);

        List<String> problems = Stream.concat(Stream.of(document), Nodes.of(Node.class).descendants(document))
                .flatMap(visitor::apply)
                .collect(Collectors.toList());

        if (problems.isEmpty()) {
            System.out.println("No problem found");
            return CommandLine.ExitCode.OK;
        } else {
            problems.forEach(System.out::println);
            return CommandLine.ExitCode.USAGE;
        }
    }

    @NotNull
    private static List<Rule<Node>> getRules() {
        return Stream.concat(Stream.of(GuidingPrinciples.values()), Stream.of(ExtendedRules.values()))
                .map(o -> (Rule<Node>) o)
                .collect(Collectors.toList());
    }
}
