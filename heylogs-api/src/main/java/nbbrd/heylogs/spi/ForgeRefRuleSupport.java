package nbbrd.heylogs.spi;

import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.util.ast.Node;
import internal.heylogs.spi.URLExtractor;
import lombok.NonNull;
import nbbrd.io.text.Parser;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Locale.ROOT;

@lombok.Builder(toBuilder = true)
public final class ForgeRefRuleSupport<L extends ForgeLink, R extends ForgeRef<L>> implements Rule {

    private final @NonNull String id;

    private final @NonNull String name;

    private final @NonNull String moduleId;

    private final @NonNull String forgeId;

    private final @NonNull ForgeRefType refType;

    @lombok.Builder.Default
    private final @NonNull Predicate<Properties> availability = properties -> true;

    @lombok.Builder.Default
    private final @NonNull RuleSeverity severity = RuleSeverity.ERROR;

    private final @NonNull ForgeRefFactory<L, R> factory;

    @lombok.Builder.Default
    private final @NonNull Predicate<L> linkPredicate = ignoreLink -> true;

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
        L expectedLink = Parser.of(((Function<URL, L>) factory::parseLink).compose(URLExtractor::urlOf)).parse(link.getUrl());
        if (expectedLink != null && (this.forgeId.equals(forgeId) || linkPredicate.test(expectedLink))) {
            R foundRef = Parser.of(factory::parseRef).parse(link.getText());
            if (foundRef == null || !foundRef.isCompatibleWith(expectedLink)) {
                R expectedRef = factory.toRef(expectedLink, foundRef);
                String foundText = foundRef == null ? link.getText().toString() : foundRef.toString();
                return RuleIssue
                        .builder()
                        .message(String.format(ROOT, "Expecting %s ref %s, found %s", refType, expectedRef, foundText))
                        .location(link)
                        .build();
            }
        }
        return NO_RULE_ISSUE;
    }

    public static <L extends ForgeLink, R extends ForgeRef<L>> @NonNull Builder<L, R> builder(
            ForgeRefFactory<L, R> factory
    ) {
        return new Builder<L, R>().factory(factory);
    }
}
