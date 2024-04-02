package internal.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.LinkNodeBase;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;
import nbbrd.design.MightBeGenerated;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.Util;
import nbbrd.heylogs.Version;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleBatch;
import nbbrd.heylogs.spi.RuleIssue;
import nbbrd.heylogs.spi.RuleSeverity;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.stream.Stream;

import static internal.heylogs.RuleSupport.linkToURL;
import static internal.heylogs.RuleSupport.nameToId;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static nbbrd.heylogs.Util.illegalArgumentToNull;

public enum ExtendedRules implements Rule {

    HTTPS {
        @Override
        public RuleIssue getRuleIssueOrNull(@NonNull Node node) {
            return node instanceof LinkNodeBase ? validateHttps((LinkNodeBase) node) : NO_RULE_ISSUE;
        }
    },
    CONSISTENT_SEPARATOR {
        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node) {
            return node instanceof Document ? validateConsistentSeparator((Document) node) : NO_RULE_ISSUE;
        }
    };

    @Override
    public @NonNull String getRuleId() {
        return nameToId(this);
    }

    @Override
    public boolean isRuleAvailable() {
        return true;
    }

    @Override
    public @NonNull RuleSeverity getRuleSeverity() {
        return RuleSeverity.ERROR;
    }

    @VisibleForTesting
    static RuleIssue validateHttps(LinkNodeBase link) {
        return linkToURL(link)
                .filter(url -> !url.getProtocol().equals("https"))
                .map(ignore -> RuleIssue
                        .builder()
                        .message("Expecting HTTPS protocol")
                        .location(link)
                        .build())
                .orElse(NO_RULE_ISSUE);
    }

    @VisibleForTesting
    static RuleIssue validateConsistentSeparator(Document doc) {
        List<Character> separators = Nodes.of(Heading.class)
                .descendants(doc)
                .filter(Version::isVersionLevel)
                .map(illegalArgumentToNull(Version::parse))
                .filter(version -> version != null && !version.isUnreleased())
                .map(Version::getSeparator)
                .distinct()
                .collect(toList());

        return separators.size() > 1
                ? RuleIssue
                .builder()
                .message("Expecting consistent version-date separator " + Util.toUnicode(separators.get(0)) + ", found " + separators.stream().map(Util::toUnicode).collect(joining(", ", "[", "]")))
                .location(doc)
                .build()
                : NO_RULE_ISSUE;
    }

    @SuppressWarnings("unused")
    @MightBeGenerated
    @ServiceProvider
    public static final class Batch implements RuleBatch {

        @Override
        public @NonNull Stream<Rule> getProviders() {
            return Stream.of(ExtendedRules.values());
        }
    }
}
