package nbbrd.heylogs;

import com.vladsch.flexmark.util.ast.Node;
import lombok.AccessLevel;

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
        return new Failure(rule.name(), message, node.getStartLineNumber() + 1, node.getStartOfLine());
    }

    public static Failure of(Rule rule, String message, int line, int column) {
        return new Failure(rule.name(), message, line, column);
    }
}
