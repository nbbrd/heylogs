package internal.heylogs.base;

import com.vladsch.flexmark.ast.BulletListItem;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.LinkNodeBase;
import com.vladsch.flexmark.util.ast.Node;
import internal.heylogs.FlexmarkIO;
import nbbrd.design.MightBePromoted;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.VersioningConfig;
import nbbrd.heylogs.spi.ForgeLinkType;
import nbbrd.heylogs.spi.ForgeSupport;
import nbbrd.heylogs.spi.RuleContext;
import nbbrd.heylogs.spi.RuleIssue;
import nbbrd.heylogs.spi.RuleSeverity;
import org.junit.jupiter.api.Test;
import tests.heylogs.spi.MockedCompareLink;
import tests.heylogs.spi.MockedForgeLink;
import tests.heylogs.spi.MockedForgeRef;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.stream.StreamSupport;

import static internal.heylogs.base.BaseVersionings.REGEX_VERSIONING;
import static internal.heylogs.base.ExtendedRules.*;
import static nbbrd.heylogs.spi.ForgeLinkType.ISSUE;
import static nbbrd.io.function.IOFunction.unchecked;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;
import static tests.heylogs.api.Sample.asHeading;
import static tests.heylogs.api.Sample.using;
import static tests.heylogs.spi.RuleAssert.assertRuleCompliance;

public class ExtendedRulesTest {

    @Test
    public void testCompliance() {
        assertRuleCompliance(new ExtendedRules.Batch());
    }

    @Test
    public void test() {
        Node sample = using("/Main.md");
        for (ExtendedRules rule : ExtendedRules.values()) {
            if (rule.getRuleSeverity() == RuleSeverity.OFF) continue;
            assertThat(Nodes.of(Node.class).descendants(sample).map(node -> rule.getRuleIssueOrNull(node, RuleContext.DEFAULT)).filter(Objects::nonNull))
                    .isEmpty();
        }
    }

    @Test
    public void testValidateHttps() {
        assertThat(Nodes.of(LinkNodeBase.class).descendants(using("/Main.md")))
                .map(ExtendedRules::validateHttps)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(LinkNodeBase.class).descendants(using("/NonHttps.md")))
                .map(ExtendedRules::validateHttps)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting HTTPS protocol").line(1).column(1).build(), atIndex(0))
                .contains(RuleIssue.builder().message("Expecting HTTPS protocol").line(2).column(7).build(), atIndex(1))
                .contains(RuleIssue.builder().message("Expecting HTTPS protocol").line(3).column(7).build(), atIndex(2))
                .contains(RuleIssue.builder().message("Expecting HTTPS protocol").line(5).column(1).build(), atIndex(3))
                .hasSize(4);
    }

    @Test
    public void testValidateConsistentSeparator() {
        assertThat(validateConsistentSeparator(using("/ErraticSeparator.md")))
                .isEqualTo(RuleIssue.builder().message("Expecting consistent version-date separator \\u002d, found [\\u002d, \\u2013, \\u2014]").line(1).column(1).build());

        assertThat(validateConsistentSeparator(using("/NonDefaultSeparator.md")))
                .isEqualTo(NO_RULE_ISSUE);
    }

    @Test
    public void testValidateUniqueHeadings() {
        assertThat(validateUniqueHeadings(using("/NonUniqueHeadings.md")))
                .isEqualTo(RuleIssue.builder().message("Heading [1.1.0] - 2019-02-15 has 2 duplicate CHANGED entries").line(5).column(1).build());
    }

    @Test
    public void testValidateNoEmptyGroup() {
        assertThat(validateNoEmptyGroup(using("/NoEmptyGroup.md")))
                .isEqualTo(RuleIssue.builder().message("Heading [1.1.0] - 2019-02-15 has no entries for CHANGED").line(17).column(1).build());
    }

    @Test
    public void testValidateNoEmptyRelease() {
        assertThat(validateNoEmptyRelease(using("/NoEmptyRelease.md")))
                .isEqualTo(RuleIssue.builder().message("Heading [1.1.0] - 2019-02-15 has no entries").line(5).column(1).build());
    }

    @Test
    public void testValidateUniqueRelease() {
        assertThat(validateUniqueRelease(using("/UniqueRelease.md")))
                .isEqualTo(RuleIssue.builder().message("Release 1.1.0 has 2 duplicates").line(5).column(1).build());
    }

    @Test
    public void testValidateImbalancedBraces() {
        assertThat(validateImbalancedBraces(using("/ImbalancedBraces.md")))
                .isEqualTo(RuleIssue.builder().message("Imbalanced braces found in '- Danish translation from [@frederikspang](https://github.com/frederikspang)].'").line(9).column(1).build());
    }

    @Test
    public void testValidateDuplicateItems() {
        assertThat(validateDuplicateItems(using("/DuplicateItems.md")))
                .extracting(RuleIssue::getMessage)
                .asString()
                .contains("Duplicate item found in version 1.1.0 across ADDED and CHANGED")
                .contains("- Danish translation from [@frederikspang](https://github.com/frederikspang).")
                .contains("appears 2 times");

        assertThat(validateDuplicateItems(using("/DuplicateItemsAcrossVersions.md")))
                .extracting(RuleIssue::getMessage)
                .asString()
                .contains("Duplicate item found across versions 1.1.0 (ADDED) and 1.0.0 (FIXED)")
                .contains("- Danish translation from [@frederikspang](https://github.com/frederikspang).")
                .contains("appears 2 times");
    }

    @Test
    public void testHasImbalancedBraces() {
        assertThat(hasImbalancedBraces("")).isFalse();
        assertThat(hasImbalancedBraces("()")).isFalse();
        assertThat(hasImbalancedBraces("{}")).isFalse();
        assertThat(hasImbalancedBraces("[]")).isFalse();
        assertThat(hasImbalancedBraces("({}[])")).isFalse();
        assertThat(hasImbalancedBraces("[{()}]")).isFalse();
        assertThat(hasImbalancedBraces("(]")).isTrue();
        assertThat(hasImbalancedBraces("{[}]")).isTrue();
        assertThat(hasImbalancedBraces("[(])")).isTrue();
        assertThat(hasImbalancedBraces("{(})")).isTrue();
        assertThat(hasImbalancedBraces("(()")).isTrue();

        // Test backticks - braces inside backticks should be ignored
        assertThat(hasImbalancedBraces("`(`")).isFalse();
        assertThat(hasImbalancedBraces("`{`")).isFalse();
        assertThat(hasImbalancedBraces("`[`")).isFalse();
        assertThat(hasImbalancedBraces("`(]`")).isFalse();
        assertThat(hasImbalancedBraces("text `{unmatched` text")).isFalse();
        assertThat(hasImbalancedBraces("text `[code]` (balanced)")).isFalse();
        assertThat(hasImbalancedBraces("text `[code]` (unbalanced")).isTrue();
        assertThat(hasImbalancedBraces("(text `[ignore]` balanced)")).isFalse();
        assertThat(hasImbalancedBraces("(text `[ignore]` unbalanced")).isTrue();
    }

    @Test
    public void testValidateVersioningFormat() {
        RuleContext withoutSemver = RuleContext.DEFAULT;
        RuleContext withSemver = RuleContext
                .builder()
                .config(Config
                        .builder()
                        .versioning(VersioningConfig.parse("regex:^\\d+\\.\\d+\\.\\d+$"))
                        .build())
                .versioning(REGEX_VERSIONING)
                .build();

        assertThat(Nodes.of(Heading.class).descendants(using("/InvalidSemver.md")))
                .map(node -> VERSIONING_FORMAT.getRuleIssueOrNull(node, withoutSemver))
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Heading.class).descendants(using("/InvalidSemver.md")))
                .map(node -> VERSIONING_FORMAT.getRuleIssueOrNull(node, withSemver))
                .filteredOn(Objects::nonNull)
                .hasSize(1)
                .contains(RuleIssue.builder().message("Invalid reference '.1.0' when using versioning 'regex:^\\d+\\.\\d+\\.\\d+$'").line(4).column(1).build());
    }

    @Test
    public void testValidateForgeRef() {
        assertThat(validateForgeRef(asLink("[stuff](invalidURL)"), RuleContext.DEFAULT))
                .describedAs("Invalid URL")
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateForgeRef(asLink("[stuff](https://issue)"), RuleContext.DEFAULT))
                .describedAs("No forge configured")
                .isEqualTo(NO_RULE_ISSUE);

        ForgeSupport forge = ForgeSupport
                .builder()
                .id("f1").name("").moduleId("")
                .knownHostPredicate(url -> true)
                .build();

        assertThat(validateForgeRef(asLink("[stuff](https://issue)"), RuleContext.builder().forge(forge).build()))
                .describedAs("No link parser")
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateForgeRef(asLink("[stuff](https://issue)"), RuleContext.builder().forge(forge.toBuilder().linkParser(ISSUE, url -> null).build()).build()))
                .describedAs("No expected link")
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateForgeRef(asLink("[stuff](https://issue)"), RuleContext.builder().forge(forge.toBuilder().linkParser(ISSUE, url -> MockedForgeLink.of(url, null)).build()).build()))
                .describedAs("No ref parser")
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateForgeRef(asLink("[stuff](https://issue)"), RuleContext.builder().forge(forge.toBuilder().linkParser(ISSUE, url -> MockedForgeLink.of(url, null)).refParser(ISSUE, ref -> MockedForgeRef.of(true)).build()).build()))
                .describedAs("Compatible ref")
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateForgeRef(asLink("[stuff](https://issue)"), RuleContext.builder().forge(forge.toBuilder().linkParser(ISSUE, url -> MockedForgeLink.of(url, null)).refParser(ISSUE, ref -> null).build()).build()))
                .describedAs("No expected ref")
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateForgeRef(asLink("[stuff](https://issue)"), RuleContext.builder().forge(forge.toBuilder().linkParser(ISSUE, url -> MockedForgeLink.of(url, MockedForgeRef.of(true))).refParser(ISSUE, ref -> null).build()).build()))
                .describedAs("No found ref")
                .isEqualTo(RuleIssue.builder().message("Expecting f1 ISSUE ref MockedForgeRef(compatibility=true), found stuff").line(1).column(1).build());

        assertThat(validateForgeRef(asLink("[stuff](https://issue)"), RuleContext.builder().forge(forge.toBuilder().linkParser(ISSUE, url -> MockedForgeLink.of(url, MockedForgeRef.of(true))).refParser(ISSUE, ref -> MockedForgeRef.of(false)).build()).build()))
                .describedAs("Incompatible ref")
                .isEqualTo(RuleIssue.builder().message("Expecting f1 ISSUE ref MockedForgeRef(compatibility=true), found MockedForgeRef(compatibility=false)").line(1).column(1).build());
    }

    @Test
    public void testValidateReleaseDate() {
        assertThat(validateReleaseDate(asHeading("## [Unreleased]"), RuleContext.DEFAULT))
                .isEqualTo(NO_RULE_ISSUE);

        LocalDate now = LocalDate.now(ZoneId.systemDefault());

        assertThat(validateReleaseDate(asHeading("## [1.0.0] - " + now), RuleContext.DEFAULT))
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateReleaseDate(asHeading("## [1.0.0] - " + now.minusDays(1)), RuleContext.DEFAULT))
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateReleaseDate(asHeading("## [1.0.0] - " + now.plusDays(1)), RuleContext.DEFAULT))
                .isEqualTo(RuleIssue.builder().message("Release date " + now.plusDays(1) + " is in the future").line(1).column(1).build());

        assertThat(validateReleaseDate(asHeading("### [1.0.0] - " + now.plusDays(1)), RuleContext.DEFAULT))
                .isEqualTo(NO_RULE_ISSUE);
    }

    @Test
    public void testValidateDotSpaceLinkStyle() {
        RuleContext invalidURL = RuleContext.DEFAULT;
        RuleContext validURL = RuleContext
                .builder()
                .forge(ForgeSupport
                        .builder()
                        .id("mocked").name("").moduleId("")
                        .knownHostPredicate(url -> true)
                        .linkParser(ISSUE, MockedForgeLink::parse)
                        .build())
                .build();

        assertThat(validateDotSpaceLinkStyle(asBulletListItem("- hello. "), invalidURL))
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateDotSpaceLinkStyle(asBulletListItem("- hello. "), validURL))
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateDotSpaceLinkStyle(asBulletListItem("- hello. [abc](http://localhost)"), invalidURL))
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateDotSpaceLinkStyle(asBulletListItem("- hello. [abc](http://localhost)"), validURL))
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateDotSpaceLinkStyle(asBulletListItem("- hello [abc](http://localhost)"), invalidURL))
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateDotSpaceLinkStyle(asBulletListItem("- hello [abc](http://localhost)"), validURL))
                .isEqualTo(RuleIssue.builder().message("Expecting '. ' before link to issue or request, found 'o '").line(1).column(9).build());

        assertThat(validateDotSpaceLinkStyle(asBulletListItem("- hello [abc](localhost)"), validURL))
                .isEqualTo(NO_RULE_ISSUE);
    }

    @Test
    public void testValidateNoOrphanRef() {
        RuleContext withoutForge = RuleContext.DEFAULT;
        RuleContext withForge = RuleContext
                .builder()
                .forge(ForgeSupport
                        .builder()
                        .id("mocked").name("").moduleId("")
                        .knownHostPredicate(url -> true)
                        .refParser(ISSUE, text -> text.toString().startsWith("#") ? MockedForgeRef.of(true) : null)
                        .build())
                .build();

        assertThat(validateNoOrphanRef(asBulletListItem("- Fixed bug."), withoutForge))
                .describedAs("Plain text ending, no issue")
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateNoOrphanRef(asBulletListItem("- Fixed bug [#123](https://github.com/org/repo/issues/123)"), withForge))
                .describedAs("Inline link, no issue")
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateNoOrphanRef(asBulletListItem("- Fixed the bug [#123]"), withoutForge))
                .describedAs("LinkRef but no forge configured, no issue")
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateNoOrphanRef(asBulletListItem("- Fixed the bug [#123]"), withForge))
                .describedAs("Orphan ForgeRef without definition")
                .isEqualTo(RuleIssue.builder().message("Orphan reference '[#123]' without explicit link").line(1).column(1).build());

        assertThat(validateNoOrphanRef(asBulletListItem("- Fixed the bug #123"), withForge))
                .describedAs("Orphan ForgeRef in plain text")
                .isEqualTo(RuleIssue.builder().message("Orphan reference '#123' without explicit link").line(1).column(1).build());

        assertThat(validateNoOrphanRef(asBulletListItem("- Fixed the bug (#123)"), withForge))
                .describedAs("Orphan ForgeRef wrapped in parentheses")
                .isEqualTo(RuleIssue.builder().message("Orphan reference '(#123)' without explicit link").line(1).column(1).build());

        assertThat(validateNoOrphanRef(asBulletListItem("- Fixed the bug {#123}"), withForge))
                .describedAs("Orphan ForgeRef wrapped in curly braces")
                .isEqualTo(RuleIssue.builder().message("Orphan reference '{#123}' without explicit link").line(1).column(1).build());

        assertThat(validateNoOrphanRef(asBulletListItem("- Fixed the bug"), withForge))
                .describedAs("Plain text with no ref token, no issue")
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(Nodes.of(BulletListItem.class).descendants(using("/OrphanRef.md"))
                .map(item -> validateNoOrphanRef(item, withForge))
                .filter(Objects::nonNull))
                .describedAs("Orphan refs in full document")
                .contains(RuleIssue.builder().message("Orphan reference '[#123]' without explicit link").line(7).column(1).build())
                .contains(RuleIssue.builder().message("Orphan reference '#456' without explicit link").line(11).column(1).build())
                .contains(RuleIssue.builder().message("Orphan reference '(#789)' without explicit link").line(15).column(1).build())
                .hasSize(3);
    }

    @Test
    public void testValidateUnknownLinkType() {
        RuleContext withoutForge = RuleContext.DEFAULT;
        RuleContext withKnownType = RuleContext
                .builder()
                .forge(ForgeSupport
                        .builder()
                        .id("mocked").name("").moduleId("")
                        .knownHostPredicate(url -> true)
                        .linkParser(ISSUE, MockedForgeLink::parse)
                        .build())
                .build();
        RuleContext withUnknownType = RuleContext
                .builder()
                .forge(ForgeSupport
                        .builder()
                        .id("mocked").name("").moduleId("")
                        .knownHostPredicate(url -> true)
                        .build())
                .build();

        assertThat(validateUnknownLinkType(asBulletListItem("- hello."), withKnownType))
                .describedAs("No link at end, no issue")
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateUnknownLinkType(asBulletListItem("- hello [abc](http://localhost)"), withoutForge))
                .describedAs("No forge configured, no issue")
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateUnknownLinkType(asBulletListItem("- hello [abc](http://localhost)"), withKnownType))
                .describedAs("Known link type, no issue")
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateUnknownLinkType(asBulletListItem("- hello [abc](http://localhost)"), withUnknownType))
                .describedAs("Unknown link type")
                .isEqualTo(RuleIssue.builder().message("Link to 'http://localhost' is of unknown type").line(1).column(9).build());

        assertThat(validateUnknownLinkType(asBulletListItem("- hello [abc](invalidURL)"), withKnownType))
                .describedAs("Invalid URL, no issue")
                .isEqualTo(NO_RULE_ISSUE);
    }

    @Test
    public void testValidateTagVersioning() {
        RuleContext baseContext = RuleContext
                .builder()
                .forge(ForgeSupport
                        .builder()
                        .id("mocked").name("").moduleId("")
                        .knownHostPredicate(url -> url.getHost().contains("github") || url.getHost().contains("host"))
                        .linkParser(ISSUE, MockedForgeLink::parse)
                        .linkParser(ForgeLinkType.COMPARE, MockedCompareLink::parse)
                        .build())
                .versioning(REGEX_VERSIONING)
                .tagging(new PrefixTagging())
                .build();

        Config config = Config
                .builder()
                .versioningOf("regex:^\\d+$")
                .taggingOf("prefix:v")
                .build();

        assertThat(config)
                .extracting(baseContext::withConfig)
                .satisfies(context -> {
                    assertThat(validateTagVersioning(asLink("[abc](http://host/compare/v1...v2)"), context))
                            .isEqualTo(NO_RULE_ISSUE);

                    assertThat(validateTagVersioning(asLink("[abc](http://host/compare/v1...HEAD)"), context))
                            .isEqualTo(NO_RULE_ISSUE);

                    assertThat(validateTagVersioning(asLink("[abc](http://host/compare/HEAD...v2)"), context))
                            .isEqualTo(NO_RULE_ISSUE);

                    assertThat(validateTagVersioning(asLink("[abc](http://host/compare/v1...v2BOOM)"), context))
                            .isEqualTo(RuleIssue.builder().message("Invalid head reference '2BOOM' when using versioning 'regex:^\\d+$'").line(1).column(1).build());

                    assertThat(validateTagVersioning(asLink("[abc](http://host/compare/v1BOOM...v2)"), context))
                            .isEqualTo(RuleIssue.builder().message("Invalid base reference '1BOOM' when using versioning 'regex:^\\d+$'").line(1).column(1).build());
                });

        for (Config c : new Config[]{
                config.toBuilder().versioningOf(null).build(),
                config.toBuilder().taggingOf(null).build()})
            assertThat(c)
                    .extracting(baseContext::withConfig)
                    .satisfies(context -> {
                        assertThat(validateTagVersioning(asLink("[abc](http://host/compare/v1...v2)"), context))
                                .isEqualTo(NO_RULE_ISSUE);

                        assertThat(validateTagVersioning(asLink("[abc](http://host/compare/v1...HEAD)"), context))
                                .isEqualTo(NO_RULE_ISSUE);

                        assertThat(validateTagVersioning(asLink("[abc](http://host/compare/HEAD...v2)"), context))
                                .isEqualTo(NO_RULE_ISSUE);

                        assertThat(validateTagVersioning(asLink("[abc](http://host/compare/v1...v2BOOM)"), context))
                                .isEqualTo(NO_RULE_ISSUE);

                        assertThat(validateTagVersioning(asLink("[abc](http://host/compare/v1BOOM...v2)"), context))
                                .isEqualTo(NO_RULE_ISSUE);
                    });

        assertThat(Nodes.of(Node.class).descendants(using("/Main.md")).map(node -> TAG_VERSIONING.getRuleIssueOrNull(node, baseContext.withConfig(config))).filter(Objects::nonNull))
                .hasSize(13)
                .allMatch(ruleIssue -> ruleIssue.getMessage().contains("Invalid base reference"));
    }

    @Test
    public void testValidateNoLinkBrackets() {
        assertThat(validateNoLinkBrackets(asBulletListItem("- hello.")))
                .describedAs("No link, no issue")
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateNoLinkBrackets(asBulletListItem("- hello [abc](https://example.com)")))
                .describedAs("Link not surrounded by brackets, no issue")
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateNoLinkBrackets(asBulletListItem("- hello ([abc](https://example.com))")))
                .describedAs("Link surrounded by parentheses")
                .isEqualTo(RuleIssue.builder().message("Expecting link without surrounding brackets").line(1).column(10).build());

        assertThat(validateNoLinkBrackets(asBulletListItem("- hello {[abc](https://example.com)}")))
                .describedAs("Link surrounded by curly braces")
                .isEqualTo(RuleIssue.builder().message("Expecting link without surrounding brackets").line(1).column(10).build());

        assertThat(validateNoLinkBrackets(asBulletListItem("- hello ([abc](https://example.com)) more")))
                .describedAs("Link in brackets but not at end of entry, no issue")
                .isEqualTo(NO_RULE_ISSUE);
    }

    @Test
    public void testValidateColumnWidth() {
        assertThat(validateColumnWidth(asBulletListItem("- Short entry")))
                .describedAs("Short entry, no issue")
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateColumnWidth(asBulletListItem("- " + repeat('a', 78))))
                .describedAs("Exactly 80 characters, no issue")
                .isEqualTo(NO_RULE_ISSUE);

        assertThat(validateColumnWidth(asBulletListItem("- " + repeat('a', 100))))
                .describedAs("Entry exceeds 80 characters")
                .extracting(RuleIssue::getMessage)
                .asString()
                .contains("Entry exceeds 80 characters");

        assertThat(validateColumnWidth(asBulletListItem("- Short text [link](https://very-long-url-that-exceeds-the-80-character-limit.com)")))
                .describedAs("Link starts before 80, no issue")
                .isEqualTo(NO_RULE_ISSUE);
    }

    @MightBePromoted
    private static String repeat(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    private static BulletListItem asBulletListItem(String text) {
        return unchecked(FlexmarkIO.newTextParser()::parseChars)
                .andThen(doc -> (BulletListItem) StreamSupport.stream(doc.getDescendants().spliterator(), false).filter(item -> item instanceof BulletListItem).findFirst().orElseThrow(IllegalArgumentException::new))
                .apply(text);
    }

    private static Link asLink(String text) {
        return unchecked(FlexmarkIO.newTextParser()::parseChars)
                .andThen(doc -> (Link) StreamSupport.stream(doc.getDescendants().spliterator(), false).filter(item -> item instanceof Link).findFirst().orElseThrow(IllegalArgumentException::new))
                .apply(text);
    }
}
