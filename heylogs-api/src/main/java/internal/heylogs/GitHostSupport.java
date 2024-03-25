package internal.heylogs;

import com.vladsch.flexmark.ast.Link;
import nbbrd.heylogs.Failure;
import nbbrd.heylogs.spi.Rule;
import nbbrd.io.text.Parser;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public final class GitHostSupport {

    private GitHostSupport() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static <L extends GitHostLink, R extends GitHostRef<L>> Failure validateRef(
            Function<? super CharSequence, L> linkParser,
            Function<? super CharSequence, R> refParser,
            Predicate<L> linkPredicate,
            Link link, Rule rule,
            BiFunction<L, R, String> message
    ) {
        L expected = Parser.of(linkParser).parse(link.getUrl());
        if (expected != null && linkPredicate.test(expected)) {
            R found = Parser.of(refParser).parse(link.getText());
            if (isNotCompatibleRef(found, expected)) {
                return Failure
                        .builder()
                        .rule(rule)
                        .message(message.apply(expected, found))
                        .location(link)
                        .build();
            }
        }
        return Rule.NO_PROBLEM;
    }

    private static <T extends GitHostLink> boolean isNotCompatibleRef(GitHostRef<T> found, T expected) {
        return found != null && !found.isCompatibleWith(expected);
    }
}
