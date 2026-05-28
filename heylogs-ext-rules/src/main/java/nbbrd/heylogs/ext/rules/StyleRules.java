package nbbrd.heylogs.ext.rules;

import com.vladsch.flexmark.ast.BulletListItem;
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
import nbbrd.heylogs.spi.*;
import nbbrd.service.ServiceProvider;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static nbbrd.heylogs.spi.RuleSupport.nameToId;

public enum StyleRules implements Rule {

    CONSISTENT_SEPARATOR {
        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull RuleContext context) {
            return node instanceof Document ? validateConsistentSeparator((Document) node) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "Consistent separator";
        }
    },
    UNIQUE_HEADINGS {
        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull RuleContext context) {
            return node instanceof Document ? validateUniqueHeadings((Document) node) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "Unique headings";
        }
    },
    IMBALANCED_BRACES {
        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull RuleContext context) {
            return node instanceof Document ? validateImbalancedBraces((Document) node) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "Imbalanced braces";
        }
    },
    COLUMN_WIDTH {
        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull RuleContext context) {
            return node instanceof BulletListItem ? validateColumnWidth((BulletListItem) node) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "Column width";
        }

        @Override
        public @NonNull RuleSeverity getRuleSeverity() {
            return RuleSeverity.OFF;
        }
    };

    @Override
    public @NonNull String getRuleId() {
        return nameToId(this);
    }

    @Override
    public @NonNull String getRuleModuleId() {
        return "rules";
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
    static RuleIssue validateConsistentSeparator(Document doc) {
        return ChangelogHeading.root(doc)
                .map(StyleRules::validateConsistentSeparator)
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
                .message("Expecting consistent version-date separator " + Util.toUnicode(separators.get(0)) + ", but also found: " + separators.subList(1, separators.size()).stream().map(Util::toUnicode).collect(joining(", ", "[", "]")))
                .location(changelog.getHeading())
                .build()
                : NO_RULE_ISSUE;
    }

    @VisibleForTesting
    static RuleIssue validateUniqueHeadings(Document doc) {
        return ChangelogHeading.root(doc)
                .map(StyleRules::validateUniqueHeadings)
                .orElse(NO_RULE_ISSUE);
    }

    private static RuleIssue validateUniqueHeadings(ChangelogHeading changelog) {
        return changelog
                .getVersions()
                .flatMap(StyleRules::validateUniqueHeadingsOnVersionNode)
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
                .collect(groupingBy(TypeOfChangeHeading::getSection, LinkedHashMap::new, counting()));
    }

    @VisibleForTesting
    static RuleIssue validateImbalancedBraces(Document doc) {
        return ChangelogHeading.root(doc)
                .map(StyleRules::validateImbalancedBraces)
                .orElse(NO_RULE_ISSUE);
    }

    private static RuleIssue validateImbalancedBraces(ChangelogHeading changelog) {
        return changelog
                .getVersions()
                .flatMap(VersionHeading::getTypeOfChanges)
                .flatMap(TypeOfChangeHeading::getBulletListItems)
                .filter(listItem -> hasImbalancedBraces(listItem.getChars().trim().toString()))
                .findFirst()
                .map(item -> RuleIssue
                        .builder()
                        .message("Imbalanced braces found in '" + RulesUtil.truncate(item.getChars().trim().toString(), 60) + "'")
                        .location(item)
                        .build())
                .orElse(NO_RULE_ISSUE);
    }

    @VisibleForTesting
    static boolean hasImbalancedBraces(String markdown) {
        final String braces = "{}[]()";
        Deque<Character> stack = new ArrayDeque<>();
        char[] chars = markdown.toCharArray();
        int i = 0;
        while (i < chars.length) {
            char c = chars[i];
            if (c == '`') {
                int backtickStart = i;
                while (i < chars.length && chars[i] == '`') i++;
                int backtickCount = i - backtickStart;
                int closing = indexOfBacktickRun(chars, i, backtickCount);
                if (closing >= 0) {
                    i = closing + backtickCount;
                }
                continue;
            }
            int idx = braces.indexOf(c);
            if (idx != -1) {
                if (idx % 2 == 0) {
                    stack.push(c);
                } else {
                    if (stack.isEmpty() || stack.pop() != braces.charAt(idx - 1)) {
                        return true;
                    }
                }
            }
            i++;
        }
        return !stack.isEmpty();
    }

    private static int indexOfBacktickRun(char[] chars, int start, int count) {
        int i = start;
        while (i <= chars.length - count) {
            if (chars[i] == '`') {
                int runStart = i;
                while (i < chars.length && chars[i] == '`') i++;
                if (i - runStart == count) return runStart;
            } else {
                i++;
            }
        }
        return -1;
    }

    @VisibleForTesting
    static @Nullable RuleIssue validateColumnWidth(@NonNull BulletListItem item) {
        String text = item.getChars().toString();
        int length = text.length();

        if (length <= 80) return NO_RULE_ISSUE;

        Node paragraph = item.getFirstChild();
        if (paragraph != null) {
            int position = 0;
            for (Node child = paragraph.getFirstChild(); child != null; child = child.getNext()) {
                int childStart = position;
                position += child.getChars().length();

                if (child instanceof com.vladsch.flexmark.ast.Link && childStart < 80) {
                    return NO_RULE_ISSUE;
                }
            }
        }

        return RuleIssue
                .builder()
                .message(String.format(java.util.Locale.ROOT, "Entry exceeds 80 characters (length: %d)", length))
                .location(item)
                .build();
    }

    @SuppressWarnings("unused")
    @DirectImpl
    @MightBeGenerated
    @ServiceProvider
    public static final class Batch implements RuleBatch {

        @Override
        public @NonNull Stream<Rule> getProviders() {
            return Stream.of(StyleRules.values());
        }
    }
}

