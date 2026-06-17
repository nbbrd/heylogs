package nbbrd.heylogs.ext.rules;

import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.ast.util.ReferenceRepository;
import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.MightBeGenerated;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.spi.*;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;
import org.jspecify.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Locale.ROOT;
import static nbbrd.heylogs.Util.illegalArgumentToNull;
import static nbbrd.heylogs.spi.RuleSupport.nameToId;
import static nbbrd.heylogs.spi.Tagging.CONVERSION_NOT_SUPPORTED;
import static nbbrd.heylogs.spi.Versioning.NO_VERSIONING_FILTER;

public enum LinkRules implements Rule {

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
    static RuleIssue validateHttps(LinkNodeBase link) {
        try {
            if (!new URL(link.getUrl().toString()).getProtocol().equals("http")) return NO_RULE_ISSUE;
        } catch (MalformedURLException ignore) {
            return NO_RULE_ISSUE;
        }
        return RuleIssue
                .builder()
                .message("Expecting HTTPS protocol")
                .location(link)
                .build();
    }

    @VisibleForTesting
    public static @Nullable RuleIssue validateTagVersioning(@NonNull LinkNodeBase link, @NonNull RuleContext context) {
        URL url = Parser.onURL().parse(link.getUrl());
        Converter<String, String> tagParser = context.findTagParserOrNull();
        java.util.function.Predicate<CharSequence> versioningPredicate = context.findVersioningPredicateOrNull();

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

    private static @Nullable Link getLastLink(BulletListItem item) {
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

    @SuppressWarnings("unused")
    @DirectImpl
    @MightBeGenerated
    @ServiceProvider
    public static final class Batch implements RuleBatch {

        @Override
        public @NonNull Stream<Rule> getProviders() {
            return Stream.of(LinkRules.values());
        }
    }
}

