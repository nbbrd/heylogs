package nbbrd.heylogs.spi;

import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.util.ast.Node;
import internal.heylogs.spi.URLExtractor;
import lombok.NonNull;
import nbbrd.io.text.Parser;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

@lombok.Builder(toBuilder = true)
public final class ForgeRefRuleSupport<L extends ForgeLink, R extends ForgeRef<L>> implements Rule {

    private final @NonNull String id;

    private final @NonNull String name;

    private final @NonNull String moduleId;

    @lombok.Builder.Default
    private final @NonNull Predicate<Properties> availability = properties -> true;

    @lombok.Builder.Default
    private final @NonNull RuleSeverity severity = RuleSeverity.ERROR;

    private final @NonNull Function<? super URL, L> linkParser;

    private final @NonNull Function<? super CharSequence, R> refParser;

    @lombok.Builder.Default
    private final @NonNull BiPredicate<L, String> linkPredicate = (ignoreLink, ignoreForgeId) -> true;

    private final @NonNull BiFunction<L, CharSequence, String> parsableMessage;

    private final @NonNull BiFunction<L, R, String> compatibleMessage;

    @Override
    public @NonNull String getRuleId() {
        return id;
    }

    @Override
    public @NonNull String getRuleName() {
        return name;
    }

    @Override
    public @NonNull String getRuleModuleId() {
        return moduleId;
    }

    @Override
    public boolean isRuleAvailable() {
        return availability.test(System.getProperties());
    }

    @Override
    public @NonNull RuleSeverity getRuleSeverity() {
        return severity;
    }

    @Override
    public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull RuleContext context) {
        return node instanceof Link ? validateLink((Link) node, context.getConfig().getForgeId()) : NO_RULE_ISSUE;
    }

    private @Nullable RuleIssue validateLink(@NonNull Link link, @Nullable String forgeId) {
        L expected = Parser.of(linkParser.compose(URLExtractor::urlOf)).parse(link.getUrl());
        if (expected != null && linkPredicate.test(expected, forgeId)) {
            R found = Parser.of(refParser).parse(link.getText());
            if (found == null) {
                return RuleIssue
                        .builder()
                        .message(parsableMessage.apply(expected, link.getText()))
                        .location(link)
                        .build();
            }
            if (!found.isCompatibleWith(expected)) {
                return RuleIssue
                        .builder()
                        .message(compatibleMessage.apply(expected, found))
                        .location(link)
                        .build();
            }
        }
        return NO_RULE_ISSUE;
    }

    public static <L extends ForgeLink, R extends ForgeRef<L>> @NonNull Builder<L, R> builder(
            Function<? super URL, L> linkParser,
            Function<? super CharSequence, R> refParser
    ) {
        return new Builder<L, R>().linkParser(linkParser).refParser(refParser);
    }
}
