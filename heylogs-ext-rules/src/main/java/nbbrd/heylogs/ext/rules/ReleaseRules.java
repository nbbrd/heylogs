package nbbrd.heylogs.ext.rules;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import internal.heylogs.ChangelogHeading;
import internal.heylogs.TypeOfChangeHeading;
import internal.heylogs.VersionHeading;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.MightBeGenerated;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.Version;
import nbbrd.heylogs.spi.*;
import nbbrd.service.ServiceProvider;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Locale.ROOT;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static nbbrd.heylogs.Util.illegalArgumentToNull;
import static nbbrd.heylogs.spi.RuleSupport.nameToId;
import static nbbrd.heylogs.spi.Versioning.NO_VERSIONING_FILTER;

public enum ReleaseRules implements Rule {

    NO_EMPTY_GROUP {
        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull RuleContext context) {
            return node instanceof Document ? validateNoEmptyGroup((Document) node) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "No empty group";
        }
    },
    NO_EMPTY_RELEASE {
        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull RuleContext context) {
            return node instanceof Document ? validateNoEmptyRelease((Document) node) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "No empty release";
        }
    },
    UNIQUE_RELEASE {
        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull RuleContext context) {
            return node instanceof Document ? validateUniqueRelease((Document) node) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "Unique release";
        }
    },
    DUPLICATE_ITEMS {
        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull RuleContext context) {
            return node instanceof Document ? validateDuplicateItems((Document) node) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "Duplicate items";
        }
    },
    VERSIONING_FORMAT {
        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull RuleContext context) {
            return node instanceof Heading
                    ? validateVersioningFormat((Heading) node, context) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "Versioning format";
        }
    },
    RELEASE_DATE {
        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull RuleContext context) {
            return node instanceof Heading ? validateReleaseDate((Heading) node, context) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "Release date";
        }

        @Override
        public @NonNull RuleSeverity getRuleSeverity() {
            return RuleSeverity.WARN;
        }
    },
    NO_VERSION_REGRESSION {
        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull RuleContext context) {
            return node instanceof Document ? validateNoVersionRegression((Document) node, context) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "No version regression";
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
    static RuleIssue validateNoEmptyGroup(Document doc) {
        return ChangelogHeading.root(doc)
                .map(ReleaseRules::validateNoEmptyGroup)
                .orElse(NO_RULE_ISSUE);
    }

    private static RuleIssue validateNoEmptyGroup(ChangelogHeading changelog) {
        return changelog
                .getVersions()
                .filter(version -> version.getSection().isReleased())
                .flatMap(ReleaseRules::validateNoEmptyGroupOnVersionNode)
                .findFirst()
                .orElse(NO_RULE_ISSUE);
    }

    private static Stream<RuleIssue> validateNoEmptyGroupOnVersionNode(VersionHeading version) {
        return version.getTypeOfChanges()
                .filter(typeOfChange -> typeOfChange.getBulletListItems().count() == 0)
                .map(typeOfChange -> RuleIssue
                        .builder()
                        .message("Heading " + version.getHeading().getText() + " has no entries for " + typeOfChange.getSection())
                        .location(typeOfChange.getHeading())
                        .build());
    }

    @VisibleForTesting
    static RuleIssue validateNoEmptyRelease(Document doc) {
        return ChangelogHeading.root(doc)
                .map(ReleaseRules::validateNoEmptyRelease)
                .orElse(NO_RULE_ISSUE);
    }

    private static RuleIssue validateNoEmptyRelease(ChangelogHeading changelog) {
        return changelog
                .getVersions()
                .filter(version -> version.getSection().isReleased())
                .filter(version -> version.getTypeOfChanges().count() == 0)
                .findFirst()
                .map(version -> RuleIssue
                        .builder()
                        .message("Heading " + version.getHeading().getText() + " has no entries")
                        .location(version.getHeading())
                        .build())
                .orElse(NO_RULE_ISSUE);
    }

    @VisibleForTesting
    static RuleIssue validateUniqueRelease(Document doc) {
        return ChangelogHeading.root(doc)
                .map(ReleaseRules::validateUniqueRelease)
                .orElse(NO_RULE_ISSUE);
    }

    private static RuleIssue validateUniqueRelease(ChangelogHeading changelog) {
        return changelog
                .getVersions()
                .collect(groupingBy(version -> version.getSection().getRef(), LinkedHashMap::new, toList()))
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

    @VisibleForTesting
    static RuleIssue validateDuplicateItems(Document doc) {
        return ChangelogHeading.root(doc)
                .map(ReleaseRules::validateDuplicateItems)
                .orElse(NO_RULE_ISSUE);
    }

    private static RuleIssue validateDuplicateItems(ChangelogHeading changelog) {
        Map<String, List<ItemLocation>> itemsByText = new LinkedHashMap<>();

        changelog.getVersions().forEach(version ->
                version.getTypeOfChanges().forEach(typeOfChange ->
                        typeOfChange.getBulletListItems().forEach(item -> {
                            String text = item.getChars().trim().toString();
                            itemsByText.computeIfAbsent(text, k -> new ArrayList<>())
                                    .add(new ItemLocation(item, version, typeOfChange));
                        })
                )
        );

        return itemsByText.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> {
                    List<ItemLocation> locations = entry.getValue();
                    ItemLocation first = locations.get(0);
                    ItemLocation second = locations.get(1);

                    String message;
                    if (first.version.getSection().getRef().equals(second.version.getSection().getRef())) {
                        message = String.format(ROOT, "Duplicate item found in version %s across %s and %s: '%s' appears %d times",
                                first.version.getSection().getRef(),
                                first.typeOfChange.getSection(),
                                second.typeOfChange.getSection(),
                                RulesUtil.truncate(entry.getKey(), 60),
                                locations.size());
                    } else {
                        message = String.format(ROOT, "Duplicate item found across versions %s (%s) and %s (%s): '%s' appears %d times",
                                first.version.getSection().getRef(),
                                first.typeOfChange.getSection(),
                                second.version.getSection().getRef(),
                                second.typeOfChange.getSection(),
                                RulesUtil.truncate(entry.getKey(), 60),
                                locations.size());
                    }

                    return RuleIssue
                            .builder()
                            .message(message)
                            .location(second.item)
                            .build();
                })
                .findFirst()
                .orElse(NO_RULE_ISSUE);
    }

    @VisibleForTesting
    static RuleIssue validateVersioningFormat(Heading heading, RuleContext context) {
        if (!Version.isVersionLevel(heading)) {
            return NO_RULE_ISSUE;
        }

        Version version = illegalArgumentToNull(Version::parse).apply(heading);

        if (version == null || version.isUnreleased()) {
            return NO_RULE_ISSUE;
        }

        String ref = version.getRef();

        Predicate<CharSequence> predicate = context.findVersioningPredicateOrNull();

        return predicate == NO_VERSIONING_FILTER || predicate.test(ref)
                ? NO_RULE_ISSUE
                : RuleIssue
                .builder()
                .message(String.format(ROOT, "Invalid reference '%s' when using versioning '%s'", ref, context.getConfig().getVersioning()))
                .location(heading)
                .build();
    }

    @VisibleForTesting
    static @Nullable RuleIssue validateReleaseDate(@NonNull Heading heading, @NonNull RuleContext context) {
        if (!Version.isVersionLevel(heading)) {
            return NO_RULE_ISSUE;
        }

        Version version = illegalArgumentToNull(Version::parse).apply(heading);

        if (version == null || version.isUnreleased()) {
            return NO_RULE_ISSUE;
        }

        LocalDate date = version.getDate();

        return date.isAfter(LocalDate.now(ZoneId.systemDefault()))
                ? RuleIssue
                .builder()
                .message(String.format(ROOT, "Release date %s is in the future", date))
                .location(heading)
                .build()
                : NO_RULE_ISSUE;
    }

    @VisibleForTesting
    static @Nullable RuleIssue validateNoVersionRegression(@NonNull Document doc, @NonNull RuleContext context) {
        Comparator<CharSequence> comparator = context.findVersioningComparatorOrNull();
        if (comparator == null) {
            return NO_RULE_ISSUE;
        }

        Function<CharSequence, String> familyMapper = context.findVersioningFamilyMapperOrNull();

        return ChangelogHeading.root(doc)
                .map(changelog -> validateNoVersionRegression(changelog, comparator, familyMapper))
                .orElse(NO_RULE_ISSUE);
    }

    private static @Nullable RuleIssue validateNoVersionRegression(@NonNull ChangelogHeading changelog, @NonNull Comparator<CharSequence> comparator, @Nullable Function<CharSequence, String> familyMapper) {
        List<VersionHeading> versions = changelog.getVersions()
                .filter(v -> v.getSection().isReleased())
                .collect(toList());

        Map<String, List<VersionHeading>> byFamily = new LinkedHashMap<>();
        for (VersionHeading version : versions) {
            String ref = version.getSection().getRef();
            String familyKey = familyMapper != null ? familyMapper.apply(ref) : ref;
            if (familyKey != null) {
                byFamily.computeIfAbsent(familyKey, k -> new ArrayList<>()).add(version);
            }
        }

        for (Map.Entry<String, List<VersionHeading>> familyEntry : byFamily.entrySet()) {
            List<VersionHeading> family = familyEntry.getValue();
            for (int i = 0; i + 1 < family.size(); i++) {
                VersionHeading current = family.get(i);
                VersionHeading next = family.get(i + 1);
                String currentRef = current.getSection().getRef();
                String nextRef = next.getSection().getRef();

                if (comparator.compare(currentRef, nextRef) < 0) {
                    return RuleIssue
                            .builder()
                            .message(String.format(ROOT, "Version '%s' is lower than '%s' in the same family",
                                    currentRef, nextRef))
                            .location(current.getHeading())
                            .build();
                }
            }
        }

        return NO_RULE_ISSUE;
    }

    private static class ItemLocation {
        final com.vladsch.flexmark.ast.BulletListItem item;
        final VersionHeading version;
        final TypeOfChangeHeading typeOfChange;

        ItemLocation(com.vladsch.flexmark.ast.BulletListItem item, VersionHeading version, TypeOfChangeHeading typeOfChange) {
            this.item = item;
            this.version = version;
            this.typeOfChange = typeOfChange;
        }
    }

    @SuppressWarnings("unused")
    @DirectImpl
    @MightBeGenerated
    @ServiceProvider
    public static final class Batch implements RuleBatch {

        @Override
        public @NonNull Stream<Rule> getProviders() {
            return Stream.of(ReleaseRules.values());
        }
    }
}



