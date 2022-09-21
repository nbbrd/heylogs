package nbbrd.heylogs;

import com.vladsch.flexmark.util.ast.Node;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Rule {

    String getName();

    Failure validate(Node node);

    @NotNull
    static List<Rule> getDefault() {
        return Stream.concat(Stream.of(GuidingPrinciples.values()), Stream.of(ExtendedRules.values()))
                .map(Rule.class::cast)
                .collect(Collectors.toList());
    }
}
