package internal.heylogs.spi;

import com.vladsch.flexmark.ast.LinkNodeBase;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.Problem;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleIssue;
import nbbrd.io.text.Parser;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public final class RuleSupport {

    private RuleSupport() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static @NonNull String nameToId(Enum<?> o) {
        return o.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    public static @NonNull Optional<URL> linkToURL(@NonNull LinkNodeBase link) {
        return Parser.onURL().parseValue(link.getUrl());
    }

    public static @NonNull Stream<Problem> problemStreamOf(@NonNull Document root, @NonNull List<Rule> rules, @NonNull Config config) {
        return Nodes.walk(root)
                .flatMap(node -> rules.stream().map(rule -> getProblemOrNull(node, rule, config)).filter(Objects::nonNull));
    }

    private static Problem getProblemOrNull(Node node, Rule rule, Config config) {
        RuleIssue ruleIssueOrNull = rule.getRuleIssueOrNull(node, config);
        return ruleIssueOrNull != null ? Problem.builder().rule(rule).issue(ruleIssueOrNull).build() : null;
    }
}
