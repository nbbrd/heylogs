package internal.heylogs.base;

import com.vladsch.flexmark.ast.BulletListItem;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.LinkNodeBase;
import com.vladsch.flexmark.util.ast.Node;
import internal.heylogs.FlexmarkIO;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.VersioningConfig;
import nbbrd.heylogs.spi.ForgeRefType;
import nbbrd.heylogs.spi.ForgeSupport;
import nbbrd.heylogs.spi.RuleContext;
import nbbrd.heylogs.spi.RuleIssue;
import org.junit.jupiter.api.Test;
import tests.heylogs.spi.MockedCompareLink;
import tests.heylogs.spi.MockedForgeLink;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.stream.StreamSupport;

import static internal.heylogs.base.BaseVersionings.REGEX_VERSIONING;
import static internal.heylogs.base.ExtendedRules.*;
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
                .hasSize(2); // FIXME: should be 3
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
                .isEqualTo(RuleIssue.builder().message("Heading [1.1.0] - 2019-02-15 has no entries for CHANGED").line(7).column(1).build());
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
                        .id("").name("").moduleId("")
                        .compareLinkFactory(url -> null)
                        .knownHostPredicate(url -> true)
                        .linkParser(ForgeRefType.ISSUE, MockedForgeLink::parse)
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
    }

    @Test
    public void testValidateTagVersioning() {
        RuleContext baseContext = RuleContext
                .builder()
                .forge(ForgeSupport
                        .builder()
                        .id("").name("").moduleId("")
                        .compareLinkFactory(MockedCompareLink::parse)
                        .knownHostPredicate(url -> url.getHost().contains("github") || url.getHost().contains("host"))
                        .linkParser(ForgeRefType.ISSUE, MockedForgeLink::parse)
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
