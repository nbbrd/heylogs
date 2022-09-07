package nbbrd.heylogs;

import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import lombok.AccessLevel;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Failure {

    @lombok.NonNull
    String rule;

    @lombok.NonNull
    String message;

    int line;

    int column;

    public static Failure of(Rule rule, String message, Node node) {
        return new Failure(rule.getName(), message, node.getStartLineNumber() + 1, node.lineColumnAtStart().getSecond() + 1);
    }

    public static Failure of(Rule rule, String message, int line, int column) {
        return new Failure(rule.getName(), message, line, column);
    }

    public static List<Failure> allOf(Document doc, List<Rule> rules) {
        return Stream.concat(Stream.of(doc), Nodes.of(Node.class).descendants(doc))
                .flatMap(node -> rules.stream().map(rule -> rule.validate(node)).filter(Objects::nonNull))
                .collect(Collectors.toList());
    }
}
