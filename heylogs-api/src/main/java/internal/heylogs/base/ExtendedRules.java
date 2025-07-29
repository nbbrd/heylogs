package internal.heylogs.base;

import com.vladsch.flexmark.ast.LinkNodeBase;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import internal.heylogs.ChangelogHeading;
import internal.heylogs.TypeOfChangeHeading;
import internal.heylogs.VersionHeading;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.MightBeGenerated;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.TypeOfChange;
import nbbrd.heylogs.Util;
import nbbrd.heylogs.Version;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleBatch;
import nbbrd.heylogs.spi.RuleIssue;
import nbbrd.heylogs.spi.RuleSeverity;
import nbbrd.service.ServiceProvider;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static internal.heylogs.spi.RuleSupport.linkToURL;
import static internal.heylogs.spi.RuleSupport.nameToId;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

public enum ExtendedRules implements Rule {

    HTTPS {
        @Override
        public RuleIssue getRuleIssueOrNull(@NonNull Node node) {
            return node instanceof LinkNodeBase ? validateHttps((LinkNodeBase) node) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "HTTPS";
        }
    },
    CONSISTENT_SEPARATOR {
        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node) {
            return node instanceof Document ? validateConsistentSeparator((Document) node) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "Consistent separator";
        }
    },
    UNIQUE_HEADINGS {
        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node) {
            return node instanceof Document ? validateUniqueHeadings((Document) node) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "Unique headings";
        }
    },
    NO_EMPTY_GROUP {
        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node) {
            return node instanceof Document ? validateNoEmptyGroup((Document) node) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "No empty group";
        }
    },
    NO_EMPTY_RELEASE {
        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node) {
            return node instanceof Document ? validateNoEmptyRelease((Document) node) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "No empty release";
        }

        @Override
        public @NonNull RuleSeverity getRuleSeverity() {
            return RuleSeverity.WARN;
        }
    },
    UNIQUE_RELEASE {
        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node) {
            return node instanceof Document ? validateUniqueRelease((Document) node) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "No empty release";
        }

        @Override
        public @NonNull RuleSeverity getRuleSeverity() {
            return RuleSeverity.WARN;
        }
    };

    @Override
    public @NonNull String getRuleId() {
        return nameToId(this);
    }

    @Override
    public @NonNull String getRuleCategory() {
        return "extension";
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
        return ChangelogHeading.root(doc)
                .map(ExtendedRules::validateConsistentSeparator)
                .orElse(NO_RULE_ISSUE);
    }

    private static RuleIssue validateConsistentSeparator(ChangelogHeading changelog) {
        List<Character> separators = changelog
                .getVersions()
                .map(VersionHeading::getSection)
                .filter(Version::isReleased)
                .map(Version::getSeparator)
                .distinct()
                .collect(toList());

        return separators.size() > 1
                ? RuleIssue
                .builder()
                .message("Expecting consistent version-date separator " + Util.toUnicode(separators.get(0)) + ", found " + separators.stream().map(Util::toUnicode).collect(joining(", ", "[", "]")))
                .location(changelog.getHeading())
                .build()
                : NO_RULE_ISSUE;
    }

    @VisibleForTesting
    static RuleIssue validateUniqueHeadings(Document doc) {
        return ChangelogHeading.root(doc)
                .map(ExtendedRules::validateUniqueHeadings)
                .orElse(NO_RULE_ISSUE);
    }

    private static RuleIssue validateUniqueHeadings(ChangelogHeading changelog) {
        return changelog
                .getVersions()
                .flatMap(ExtendedRules::validateUniqueHeadingsOnVersionNode)
                .findFirst()
                .orElse(NO_RULE_ISSUE);
    }

    private static Stream<RuleIssue> validateUniqueHeadingsOnVersionNode(VersionHeading version) {
        return countByTypeOfChange(version)
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(entry -> getDuplicationIssue(version, entry.getKey(), entry.getValue()));
    }

    private static RuleIssue getDuplicationIssue(VersionHeading version, TypeOfChange typeOfChange, long count) {
        return RuleIssue
                .builder()
                .message("Heading " + version.getHeading().getText() + " has " + count + " duplicate " + typeOfChange + " entries")
                .location(version.getHeading())
                .build();
    }

    private static Map<TypeOfChange, Long> countByTypeOfChange(VersionHeading version) {
        return version.getTypeOfChanges()
                .collect(groupingBy(TypeOfChangeHeading::getSection, counting()));
    }

    @VisibleForTesting
    static RuleIssue validateNoEmptyGroup(Document doc) {
        return ChangelogHeading.root(doc)
                .map(ExtendedRules::validateNoEmptyGroup)
                .orElse(NO_RULE_ISSUE);
    }

    private static RuleIssue validateNoEmptyGroup(ChangelogHeading changelog) {
        return changelog
                .getVersions()
                .flatMap(ExtendedRules::validateNoEmptyGroupOnVersionNode)
                .findFirst()
                .orElse(NO_RULE_ISSUE);
    }

    private static Stream<RuleIssue> validateNoEmptyGroupOnVersionNode(VersionHeading version) {
        return version.getTypeOfChanges()
                .collect(toMap(identity(), typeOfChange -> typeOfChange.getBulletListItems().count()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() == 0)
                .map(entry -> RuleIssue
                        .builder()
                        .message("Heading " + version.getHeading().getText() + " has no entries for " + entry.getKey().getSection())
                        .location(entry.getKey().getHeading())
                        .build());
    }

    @VisibleForTesting
    static RuleIssue validateNoEmptyRelease(Document doc) {
        return ChangelogHeading.root(doc)
                .map(ExtendedRules::validateNoEmptyRelease)
                .orElse(NO_RULE_ISSUE);
    }

    private static RuleIssue validateNoEmptyRelease(ChangelogHeading changelog) {
        return changelog
                .getVersions()
                .filter(version -> version.getSection().isReleased())
                .collect(toMap(identity(), version -> version.getTypeOfChanges().count()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() == 0)
                .map(entry -> RuleIssue
                        .builder()
                        .message("Heading " + entry.getKey().getHeading().getText() + " has no entries")
                        .location(entry.getKey().getHeading())
                        .build())
                .findFirst()
                .orElse(NO_RULE_ISSUE);
    }

    @VisibleForTesting
    static RuleIssue validateUniqueRelease(Document doc) {
        return ChangelogHeading.root(doc)
                .map(ExtendedRules::validateUniqueRelease)
                .orElse(NO_RULE_ISSUE);
    }

    private static RuleIssue validateUniqueRelease(ChangelogHeading changelog) {
        return changelog
                .getVersions()
                .collect(groupingBy(version -> version.getSection().getRef(), toList()))
                .entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .findFirst()
                .map(entry -> RuleIssue
                        .builder()
                        .message("Release " + entry.getKey() + " has " + entry.getValue().size() + " duplicates")
                        .location(entry.getValue().get(0).getHeading())
                        .build())
                .orElse(NO_RULE_ISSUE);
    }

    @SuppressWarnings("unused")
    @DirectImpl
    @MightBeGenerated
    @ServiceProvider
    public static final class Batch implements RuleBatch {

        @Override
        public @NonNull Stream<Rule> getProviders() {
            return Stream.of(ExtendedRules.values());
        }
    }
}
