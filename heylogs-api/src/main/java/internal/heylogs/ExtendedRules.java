package internal.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.LinkNodeBase;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;
import nbbrd.design.MightBeGenerated;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.Failure;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.Util;
import nbbrd.heylogs.Version;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleBatch;
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
        public Failure validate(@NonNull Node node) {
            return node instanceof LinkNodeBase ? validateHttps((LinkNodeBase) node) : NO_PROBLEM;
        }
    },
    CONSISTENT_SEPARATOR {
        @Override
        public @Nullable Failure validate(@NonNull Node node) {
            return node instanceof Document ? validateConsistentSeparator((Document) node) : NO_PROBLEM;
        }
    };

    @Override
    public @NonNull String getId() {
        return nameToId(this);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @VisibleForTesting
    static Failure validateHttps(LinkNodeBase link) {
        return linkToURL(link)
                .filter(url -> !url.getProtocol().equals("https"))
                .map(ignore -> Failure
                        .builder()
                        .rule(HTTPS)
                        .message("Expecting HTTPS protocol")
                        .location(link)
                        .build())
                .orElse(NO_PROBLEM);
    }

    @VisibleForTesting
    static Failure validateConsistentSeparator(Document doc) {
        List<Character> separators = Nodes.of(Heading.class)
                .descendants(doc)
                .filter(Version::isVersionLevel)
                .map(illegalArgumentToNull(Version::parse))
                .filter(version -> version != null && !version.isUnreleased())
                .map(Version::getSeparator)
                .distinct()
                .collect(toList());

        return separators.size() > 1
                ? Failure
                .builder()
                .rule(CONSISTENT_SEPARATOR)
                .message("Expecting consistent version-date separator " + Util.toUnicode(separators.get(0)) + ", found " + separators.stream().map(Util::toUnicode).collect(joining(", ", "[", "]")))
                .location(doc)
                .build()
                : NO_PROBLEM;
    }

    @MightBeGenerated
    @ServiceProvider
    public static final class Batch implements RuleBatch {

        @Override
        public @NonNull Stream<Rule> getProviders() {
            return Stream.of(ExtendedRules.values());
        }
    }
}
