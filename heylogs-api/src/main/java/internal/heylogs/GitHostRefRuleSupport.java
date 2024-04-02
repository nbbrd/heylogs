package internal.heylogs;

import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;
import nbbrd.heylogs.Failure;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleSeverity;
import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

@lombok.Builder(toBuilder = true)
public final class GitHostRefRuleSupport<L extends GitHostLink, R extends GitHostRef<L>> implements Rule {

    private final @NonNull String id;

    @lombok.Builder.Default
    private final @NonNull Predicate<Properties> availability = properties -> true;

    @lombok.Builder.Default
    private final @NonNull RuleSeverity severity = RuleSeverity.ERROR;

    private final @NonNull Function<? super CharSequence, L> linkParser;

    private final @NonNull Function<? super CharSequence, R> refParser;

    @lombok.Builder.Default
    private final @NonNull Predicate<L> linkPredicate = ignore -> true;

    private final @NonNull BiFunction<L, R, String> message;

    @Override
    public @NonNull String getId() {
        return id;
    }

    @Override
    public boolean isAvailable() {
        return availability.test(System.getProperties());
    }

    @Override
    public @NonNull RuleSeverity getRuleSeverity() {
        return severity;
    }

    @Override
    public @Nullable Failure validate(@NonNull Node node) {
        return node instanceof Link ? validateLink((Link) node) : NO_PROBLEM;
    }

    private @Nullable Failure validateLink(@NonNull Link link) {
        L expected = Parser.of(linkParser).parse(link.getUrl());
        if (expected != null && linkPredicate.test(expected)) {
            R found = Parser.of(refParser).parse(link.getText());
            if (isNotCompatibleRef(found, expected)) {
                return Failure
                        .builder()
                        .ruleId(id)
                        .ruleSeverity(severity)
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

    public static <L extends GitHostLink, R extends GitHostRef<L>> @NonNull Builder<L, R> builder(
            Function<? super CharSequence, L> linkParser,
            Function<? super CharSequence, R> refParser
    ) {
        return new Builder<L, R>().linkParser(linkParser).refParser(refParser);
    }
}
