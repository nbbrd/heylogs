package internal.heylogs.base;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Reference;
import com.vladsch.flexmark.ast.util.ReferenceRepository;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.MightBeGenerated;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.*;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleBatch;
import nbbrd.heylogs.spi.RuleIssue;
import nbbrd.heylogs.spi.RuleSeverity;
import nbbrd.service.ServiceProvider;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static internal.heylogs.spi.RuleSupport.nameToId;
import static nbbrd.heylogs.Util.illegalArgumentToNull;

public enum GuidingPrinciples implements Rule {

    FOR_HUMANS {
        @Override
        public RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull Config config) {
            return node instanceof Document ? validateForHumans((Document) node) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "For humans";
        }
    },
    ALL_H2_CONTAIN_A_VERSION {
        @Override
        public RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull Config config) {
            return node instanceof Heading ? validateAllH2ContainAVersion((Heading) node) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "All H2 contain a version";
        }
    },
    TYPE_OF_CHANGES_GROUPED {
        @Override
        public RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull Config config) {
            return node instanceof Heading ? validateTypeOfChangesGrouped((Heading) node) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "Type of changes grouped";
        }
    },
    LINKABLE {
        @Override
        public RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull Config config) {
            return node instanceof Heading ? validateLinkable((Heading) node) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "Linkable";
        }
    },
    LATEST_VERSION_FIRST {
        @Override
        public RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull Config config) {
            return node instanceof Document ? validateLatestVersionFirst((Document) node) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "Latest version first";
        }
    },
    DATE_DISPLAYED {
        @Override
        public RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull Config config) {
            return NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "Date displayed";
        }
    };

    @Override
    public @NonNull String getRuleId() {
        return nameToId(this);
    }

    @Override
    public @NonNull String getRuleCategory() {
        return "main";
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
    static RuleIssue validateForHumans(@NonNull Document document) {
        List<Heading> headings = Nodes.of(Heading.class)
                .descendants(document)
                .filter(Changelog::isChangelogLevel)
                .collect(Collectors.toList());

        switch (headings.size()) {
            case 0:
                return RuleIssue
                        .builder()
                        .message("Missing Changelog heading")
                        .location(document)
                        .build();
            case 1:
                try {
                    Changelog.parse(headings.get(0));
                    return NO_RULE_ISSUE;
                } catch (IllegalArgumentException ex) {
                    return RuleIssue
                            .builder()
                            .message(ex.getMessage())
                            .location(document)
                            .build();
                }
            default:
                return RuleIssue
                        .builder()
                        .message("Too many Changelog headings")
                        .location(document)
                        .build();
        }
    }

    @VisibleForTesting
    static RuleIssue validateAllH2ContainAVersion(@NonNull Heading heading) {
        if (!Version.isVersionLevel(heading)) {
            return NO_RULE_ISSUE;
        }
        try {
            Version.parse(heading);
        } catch (IllegalArgumentException ex) {
            return RuleIssue
                    .builder()
                    .message(ex.getMessage())
                    .location(heading)
                    .build();
        }
        return NO_RULE_ISSUE;
    }

    @VisibleForTesting
    static RuleIssue validateTypeOfChangesGrouped(@NonNull Heading heading) {
        if (!TypeOfChange.isTypeOfChangeLevel(heading)) {
            return NO_RULE_ISSUE;
        }
        try {
            TypeOfChange.parse(heading);
        } catch (IllegalArgumentException ex) {
            return RuleIssue
                    .builder()
                    .message(ex.getMessage())
                    .location(heading)
                    .build();
        }
        return NO_RULE_ISSUE;
    }

    @VisibleForTesting
    static RuleIssue validateLinkable(@NonNull Heading heading) {
        if (!Version.isVersionLevel(heading)) {
            return NO_RULE_ISSUE;
        }

        try {
            Version version = Version.parse(heading);

            ReferenceRepository repository = Parser.REFERENCES.get(heading.getDocument());
            String normalizeRef = repository.normalizeKey(version.getRef());
            Reference reference = repository.get(normalizeRef);

            return reference == null
                    ? RuleIssue
                    .builder()
                    .message("Missing reference '" + version.getRef() + "'")
                    .location(heading)
                    .build()
                    : NO_RULE_ISSUE;
        } catch (IllegalArgumentException ex) {
            return NO_RULE_ISSUE;
        }
    }

    @VisibleForTesting
    static RuleIssue validateLatestVersionFirst(@NonNull Document doc) {
        List<VersionNode> versions = VersionNode.allOf(doc);

        Comparator<VersionNode> comparator = Comparator.comparing((VersionNode item) -> item.getVersion().getDate()).reversed();
        VersionNode unsortedItem = getFirstUnsortedItem(versions, comparator);
        return unsortedItem != null
                ? RuleIssue
                .builder()
                .message("Versions not sorted")
                .location(unsortedItem.getNode())
                .build()
                : NO_RULE_ISSUE;
    }

    @lombok.Value
    private static class VersionNode {
        Version version;
        Node node;

        static VersionNode parse(Heading node) {
            return new VersionNode(Version.parse(node), node);
        }

        static List<VersionNode> allOf(Document doc) {
            return Nodes.of(Heading.class)
                    .descendants(doc)
                    .filter(Version::isVersionLevel)
                    .map(illegalArgumentToNull(VersionNode::parse))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

    }

    private static <T> T getFirstUnsortedItem(List<T> list, Comparator<T> comparator) {
        if ((list.isEmpty()) || list.size() == 1) {
            return null;
        }

        Iterator<T> iterator = list.iterator();
        T current, previous = iterator.next();
        while (iterator.hasNext()) {
            current = iterator.next();
            if (comparator.compare(previous, current) > 0) {
                return current;
            }
            previous = current;
        }
        return null;
    }

    @SuppressWarnings("unused")
    @DirectImpl
    @MightBeGenerated
    @ServiceProvider
    public static final class Batch implements RuleBatch {

        @Override
        public @NonNull Stream<Rule> getProviders() {
            return Stream.of(GuidingPrinciples.values());
        }
    }
}
