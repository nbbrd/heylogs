package internal.heylogs.base;

import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.ast.util.ReferenceRepository;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import internal.heylogs.ChangelogHeading;
import internal.heylogs.TypeOfChangeHeading;
import internal.heylogs.VersionHeading;
import internal.heylogs.spi.URLExtractor;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.MightBeGenerated;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.TypeOfChange;
import nbbrd.heylogs.Util;
import nbbrd.heylogs.Version;
import nbbrd.heylogs.spi.*;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;
import org.jspecify.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Locale.ROOT;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static nbbrd.heylogs.Util.illegalArgumentToNull;
import static nbbrd.heylogs.spi.RuleSupport.nameToId;
import static nbbrd.heylogs.spi.Tagging.CONVERSION_NOT_SUPPORTED;
import static nbbrd.heylogs.spi.Versioning.NO_VERSIONING_FILTER;

public enum ExtendedRules implements Rule {

    HTTPS {
        @Override
        public RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull RuleContext context) {
            return node instanceof LinkNodeBase ? validateHttps((LinkNodeBase) node) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "HTTPS";
        }
    },
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
    FORGE_REF {
        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull RuleContext context) {
            return node instanceof Link ? validateForgeRef((Link) node, context) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "Forge reference";
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
    DOT_SPACE_LINK_STYLE {;

        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull RuleContext context) {
            return node instanceof BulletListItem ? validateDotSpaceLinkStyle((BulletListItem) node, context) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "Dot-space-link style";
        }

        @Override
        public @NonNull RuleSeverity getRuleSeverity() {
            return RuleSeverity.OFF;
        }
    },
    UNKNOWN_LINK_TYPE {
        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull RuleContext context) {
            return node instanceof BulletListItem ? validateUnknownLinkType((BulletListItem) node, context) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "Unknown link type";
        }

        @Override
        public @NonNull RuleSeverity getRuleSeverity() {
            return RuleSeverity.WARN;
        }
    },
    TAG_VERSIONING {
        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull RuleContext context) {
            return node instanceof LinkNodeBase ? validateTagVersioning((LinkNodeBase) node, context) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "Tag versioning";
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
    NO_ORPHAN_REF {
        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull RuleContext context) {
            return node instanceof BulletListItem ? validateNoOrphanRef((BulletListItem) node, context) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "No orphan ref";
        }
    },
    NO_LINK_BRACKETS {
        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull RuleContext context) {
            return node instanceof BulletListItem ? validateNoLinkBrackets((BulletListItem) node) : NO_RULE_ISSUE;
        }

        @Override
        public @NonNull String getRuleName() {
            return "No link brackets";
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
        return "api";
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
        try {
            if (new URL(link.getUrl().toString()).getProtocol().equals("https")) return NO_RULE_ISSUE;
        } catch (MalformedURLException ignore) {
        }
        return RuleIssue
                .builder()
                .message("Expecting HTTPS protocol")
                .location(link)
                .build();
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
                .filter(version -> version.getSection().isReleased())
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

    @VisibleForTesting
    static RuleIssue validateImbalancedBraces(Document doc) {
        return ChangelogHeading.root(doc)
                .map(ExtendedRules::validateImbalancedBraces)
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
                        .message("Imbalanced braces found in '" + item.getChars().trim() + "'")
                        .location(item)
                        .build())
                .orElse(NO_RULE_ISSUE);
    }

    @VisibleForTesting
    static boolean hasImbalancedBraces(String markdown) {
        final String braces = "{}[]()";
        Deque<Character> stack = new ArrayDeque<>();
        boolean insideBackticks = false;

        for (char c : markdown.toCharArray()) {
            if (c == '`') {
                insideBackticks = !insideBackticks;
                continue;
            }

            if (insideBackticks) {
                continue; // skip braces inside backticks
            }

            int idx = braces.indexOf(c);
            if (idx == -1) continue;
            if (idx % 2 == 0) { // opening brace
                stack.push(c);
            } else { // closing brace
                if (stack.isEmpty() || stack.pop() != braces.charAt(idx - 1)) {
                    return true; // imbalance found
                }
            }
        }
        return !stack.isEmpty(); // true if any unmatched opening braces remain
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
    public static @Nullable RuleIssue validateForgeRef(@NonNull Link link, @NonNull RuleContext context) {
        URL url = Parser.onURL().parse(link.getUrl());
        if (url != null) {
            for (Forge forge : context.findAllForges(url)) {
                for (ForgeLinkType type : ForgeLinkType.values()) {
                    ForgeLinkParser linkParser = forge.getLinkParser(type);
                    ForgeLink expectedLink = linkParser != null ? linkParser.parseForgeLinkOrNull(url) : null;
                    if (expectedLink != null) {
                        ForgeRefParser refParser = forge.getRefParser(type);
                        ForgeRef foundRef = refParser != null ? refParser.parseForgeRefOrNull(link.getText()) : null;
                        if (foundRef == null || !foundRef.isCompatibleWith(expectedLink)) {
                            ForgeRef expectedRef = expectedLink.toRef(foundRef);
                            if (expectedRef != null) {
                                String foundText = foundRef == null ? link.getText().toString() : foundRef.toString();
                                return RuleIssue
                                        .builder()
                                        .message(String.format(ROOT, "Expecting %s %s ref %s, found %s", forge.getForgeId(), type, expectedRef, foundText))
                                        .location(link)
                                        .build();
                            }
                        }
                    }
                }

            }
        }
        return NO_RULE_ISSUE;
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
    static @Nullable RuleIssue validateDotSpaceLinkStyle(@NonNull BulletListItem item, @NonNull RuleContext context) {
        Link lastLink = getLastLink(item);

        if (lastLink != null && isIssueOrMergeLink(context, lastLink)) {
            Node text = lastLink.getPrevious();
            if (text instanceof Text && !text.getChars().endsWith(". ")) {
                return RuleIssue
                        .builder()
                        .message("Expecting '. ' before link to issue or request, found '" + text.getChars().subSequence(text.getChars().length() - Math.min(2, text.getChars().length()), text.getChars().length()) + "'")
                        .location(lastLink)
                        .build();
            }
        }

        return NO_RULE_ISSUE;
    }

    private static Link getLastLink(BulletListItem item) {
        Node lastLink = item;
        while (lastLink != null && !(lastLink instanceof Link)) {
            lastLink = lastLink.getLastChild();
        }
        return (Link) lastLink;
    }

    private static boolean isIssueOrMergeLink(RuleContext context, Link x) {
        URL url = illegalArgumentToNull(URLExtractor::urlOf).apply(x.getUrl());
        return url != null && context.getForges()
                .stream()
                .flatMap(forge -> Stream.of(forge.getLinkParser(ForgeLinkType.ISSUE), forge.getLinkParser(ForgeLinkType.REQUEST)))
                .filter(Objects::nonNull)
                .anyMatch(linkParser -> linkParser.parseForgeLinkOrNull(url) != null);
    }

    @VisibleForTesting
    static @Nullable RuleIssue validateUnknownLinkType(@NonNull BulletListItem item, @NonNull RuleContext context) {
        Link lastLink = getLastLink(item);
        if (lastLink == null) return NO_RULE_ISSUE;

        URL url = Parser.onURL().parse(lastLink.getUrl());
        if (url == null) return NO_RULE_ISSUE;

        List<Forge> forges = context.findAllForges(url);
        if (forges.isEmpty()) return NO_RULE_ISSUE;

        boolean isKnownType = forges.stream()
                .flatMap(forge -> Stream.of(ForgeLinkType.values()).map(forge::getLinkParser).filter(Objects::nonNull))
                .anyMatch(parser -> parser.parseForgeLinkOrNull(url) != null);

        return isKnownType
                ? NO_RULE_ISSUE
                : RuleIssue
                .builder()
                .message(String.format(ROOT, "Link to '%s' is of unknown type", url))
                .location(lastLink)
                .build();
    }

    @VisibleForTesting
    public static @Nullable RuleIssue validateTagVersioning(@NonNull LinkNodeBase link, @NonNull RuleContext context) {
        URL url = Parser.onURL().parse(link.getUrl());
        Converter<String, String> tagParser = context.findTagParserOrNull();
        Predicate<CharSequence> versioningPredicate = context.findVersioningPredicateOrNull();

        if (url != null && tagParser != CONVERSION_NOT_SUPPORTED && versioningPredicate != NO_VERSIONING_FILTER) {
            for (Forge forge : context.findAllForges(url)) {
                CompareLinkParser compareLinkParser = forge.getCompareLinkParser();
                if (compareLinkParser != null) {
                    CompareLink compareLink = compareLinkParser.parseForgeLinkOrNull(url);
                    if (compareLink != null) {
                        String baseVersion = tagParser.applyOrNull(compareLink.getCompareBaseRef());
                        if (baseVersion != null && !versioningPredicate.test(baseVersion)) {
                            return RuleIssue
                                    .builder()
                                    .message(String.format(ROOT, "Invalid base reference '%s' when using versioning '%s'", baseVersion, context.getConfig().getVersioning()))
                                    .location(link)
                                    .build();
                        }
                        String headVersion = tagParser.applyOrNull(compareLink.getCompareHeadRef());
                        if (headVersion != null && !versioningPredicate.test(headVersion)) {
                            return RuleIssue
                                    .builder()
                                    .message(String.format(ROOT, "Invalid head reference '%s' when using versioning '%s'", headVersion, context.getConfig().getVersioning()))
                                    .location(link)
                                    .build();
                        }
                    }
                }
            }
        }
        return NO_RULE_ISSUE;
    }

    @VisibleForTesting
    static RuleIssue validateDuplicateItems(Document doc) {
        return ChangelogHeading.root(doc)
                .map(ExtendedRules::validateDuplicateItems)
                .orElse(NO_RULE_ISSUE);
    }

    private static RuleIssue validateDuplicateItems(ChangelogHeading changelog) {
        // Collect all items across all versions and type of changes
        Map<String, List<ItemLocation>> itemsByText = new HashMap<>();

        changelog.getVersions().forEach(version ->
                version.getTypeOfChanges().forEach(typeOfChange ->
                        typeOfChange.getBulletListItems().forEach(item -> {
                            String text = item.getChars().trim().toString();
                            itemsByText.computeIfAbsent(text, k -> new ArrayList<>())
                                    .add(new ItemLocation(item, version, typeOfChange));
                        })
                )
        );

        // Find first duplicate across all items
        return itemsByText.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> {
                    List<ItemLocation> locations = entry.getValue();
                    ItemLocation first = locations.get(0);
                    ItemLocation second = locations.get(1);

                    String message;
                    if (first.version.getSection().getRef().equals(second.version.getSection().getRef())) {
                        // Same version, different type of change
                        message = String.format(ROOT, "Duplicate item found in version %s across %s and %s: '%s' appears %d times",
                                first.version.getSection().getRef(),
                                first.typeOfChange.getSection(),
                                second.typeOfChange.getSection(),
                                entry.getKey(),
                                locations.size());
                    } else {
                        // Different versions
                        message = String.format(ROOT, "Duplicate item found across versions %s (%s) and %s (%s): '%s' appears %d times",
                                first.version.getSection().getRef(),
                                first.typeOfChange.getSection(),
                                second.version.getSection().getRef(),
                                second.typeOfChange.getSection(),
                                entry.getKey(),
                                locations.size());
                    }

                    return RuleIssue
                            .builder()
                            .message(message)
                            .location(second.item) // Point to the second occurrence
                            .build();
                })
                .findFirst()
                .orElse(NO_RULE_ISSUE);
    }

    @VisibleForTesting
    static @Nullable RuleIssue validateNoOrphanRef(@NonNull BulletListItem item, @NonNull RuleContext context) {
        Node paragraph = item.getLastChild();
        if (paragraph == null) return NO_RULE_ISSUE;

        Node lastInline = paragraph.getLastChild();

        if (lastInline instanceof LinkRef) {
            LinkRef linkRef = (LinkRef) lastInline;

            if (!matchesForgeRef(linkRef.getReference(), context)) return NO_RULE_ISSUE;

            ReferenceRepository repository = com.vladsch.flexmark.parser.Parser.REFERENCES.get(item.getDocument());

            String normalizedKey = repository.normalizeKey(linkRef.getReference());
            Reference reference = repository.get(normalizedKey);

            return reference == null
                    ? RuleIssue
                    .builder()
                    .message("Orphan reference '[" + linkRef.getReference() + "]' without explicit link")
                    .location(item)
                    .build()
                    : NO_RULE_ISSUE;
        }

        if (lastInline instanceof Text) {
            String content = lastInline.getChars().trim().toString();
            if (content.isEmpty()) return NO_RULE_ISSUE;

            String[] tokens = content.split("\\s+");
            String lastToken = tokens[tokens.length - 1];

            return isOrphanRefToken(lastToken, context)
                    ? RuleIssue
                    .builder()
                    .message("Orphan reference '" + lastToken + "' without explicit link")
                    .location(item)
                    .build()
                    : NO_RULE_ISSUE;
        }

        return NO_RULE_ISSUE;
    }

    private static final char[][] BRACKET_WRAPPERS = {{'(', ')'}, {'{', '}'}};

    private static boolean isOrphanRefToken(@NonNull String token, @NonNull RuleContext context) {
        if (matchesForgeRef(token, context)) return true;
        for (char[] wrapper : BRACKET_WRAPPERS) {
            if (token.length() > 2 && token.charAt(0) == wrapper[0] && token.charAt(token.length() - 1) == wrapper[1]) {
                if (matchesForgeRef(token.substring(1, token.length() - 1), context)) return true;
            }
        }
        return false;
    }

    private static boolean matchesForgeRef(@NonNull CharSequence candidate, @NonNull RuleContext context) {
        return context.getForges().stream()
                .flatMap(forge -> Stream.of(ForgeLinkType.values()).map(forge::getRefParser).filter(Objects::nonNull))
                .anyMatch(refParser -> refParser.parseForgeRefOrNull(candidate) != null);
    }

    @VisibleForTesting
    static @Nullable RuleIssue validateNoLinkBrackets(@NonNull BulletListItem item) {
        Node paragraph = item.getLastChild();
        if (paragraph == null) return NO_RULE_ISSUE;

        Node lastChild = paragraph.getLastChild();
        if (!(lastChild instanceof Text)) return NO_RULE_ISSUE;

        String closingText = lastChild.getChars().trim().toString();
        if (!closingText.equals(")") && !closingText.equals("]") && !closingText.equals("}")) return NO_RULE_ISSUE;

        Node prev = lastChild.getPrevious();
        if (!(prev instanceof Link)) return NO_RULE_ISSUE;

        Link link = (Link) prev;
        Node beforeLink = link.getPrevious();
        if (!(beforeLink instanceof Text)) return NO_RULE_ISSUE;

        CharSequence beforeText = beforeLink.getChars();
        if (beforeText.length() == 0) return NO_RULE_ISSUE;

        char lastChar = beforeText.charAt(beforeText.length() - 1);
        char expectedOpening = closingText.equals(")") ? '(' : closingText.equals("]") ? '[' : '{';

        return lastChar == expectedOpening
                ? RuleIssue
                .builder()
                .message("Expecting link without surrounding brackets")
                .location(link)
                .build()
                : NO_RULE_ISSUE;
    }

    @VisibleForTesting
    static @Nullable RuleIssue validateColumnWidth(@NonNull BulletListItem item) {
        String text = item.getChars().toString();
        int length = text.length();

        if (length <= 80) return NO_RULE_ISSUE;

        // Check if a link/URL starts before position 80
        Node paragraph = item.getFirstChild();
        if (paragraph != null) {
            int position = 0;
            for (Node child = paragraph.getFirstChild(); child != null; child = child.getNext()) {
                int childStart = position;
                position += child.getChars().length();

                if (child instanceof Link && childStart < 80) {
                    return NO_RULE_ISSUE;
                }
            }
        }

        return RuleIssue
                .builder()
                .message(String.format(ROOT, "Entry exceeds 80 characters (length: %d)", length))
                .location(item)
                .build();
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

        // Group by family using the family mapper if available
        Map<String, List<VersionHeading>> byFamily = new LinkedHashMap<>();
        for (VersionHeading version : versions) {
            String ref = version.getSection().getRef();
            String familyKey = familyMapper != null ? familyMapper.apply(ref) : ref;
            if (familyKey != null) {
                byFamily.computeIfAbsent(familyKey, k -> new ArrayList<>()).add(version);
            }
        }

        // Within each family (document order = newest first), check versions are descending
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
        final BulletListItem item;
        final VersionHeading version;
        final TypeOfChangeHeading typeOfChange;

        ItemLocation(BulletListItem item, VersionHeading version, TypeOfChangeHeading typeOfChange) {
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
            return Stream.of(ExtendedRules.values());
        }
    }
}
