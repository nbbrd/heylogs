package nbbrd.heylogs.cli;

import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import internal.heylogs.cli.MarkdownInputOptions;
import nbbrd.heylogs.*;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Command(name = "check")
public final class CheckCommand implements Callable<Integer> {

    @CommandLine.Mixin
    private MarkdownInputOptions input;

    @Override
    public Integer call() throws Exception {
        Document document = input.read();
        return check(document);
    }

    private Integer check(Document document) {
        List<Rule> rules = getRules();
        System.out.println("Using rules: " + rules);

        List<Failure> problems = getFailures(document, rules);

        if (problems.isEmpty()) {
            System.out.println("No problem found");
            return CommandLine.ExitCode.OK;
        } else {
            problems
                    .stream()
                    .map(failure -> "Invalid node at line " + failure.getLine() + ": " + failure.getMessage())
                    .forEach(System.out::println);
            return CommandLine.ExitCode.USAGE;
        }
    }

    @NotNull
    private static List<Rule> getRules() {
        return Stream.concat(Stream.of(GuidingPrinciples.values()), Stream.of(ExtendedRules.values()))
                .map(Rule.class::cast)
                .collect(Collectors.toList());
    }

    private static List<Failure> getFailures(Document doc, List<Rule> rules) {
        return Stream.concat(Stream.of(doc), Nodes.of(Node.class).descendants(doc))
                .flatMap(node -> rules.stream().map(rule -> rule.validate(node)).filter(Objects::nonNull))
                .collect(Collectors.toList());
    }
}
