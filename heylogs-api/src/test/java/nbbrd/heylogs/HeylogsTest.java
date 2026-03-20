package nbbrd.heylogs;

import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import internal.heylogs.FlexmarkIO;
import internal.heylogs.base.BaseVersionings;
import internal.heylogs.base.StylishFormat;
import lombok.NonNull;
import nbbrd.heylogs.spi.*;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import tests.heylogs.spi.MockedCompareLink;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static java.util.Collections.singletonList;
import static nbbrd.heylogs.Heylogs.FIRST_FORMAT_AVAILABLE;
import static nbbrd.heylogs.Version.HYPHEN;
import static nbbrd.heylogs.spi.RuleSeverity.ERROR;
import static nbbrd.io.function.IOFunction.unchecked;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static tests.heylogs.api.Sample.using;

public class HeylogsTest {

    @Test
    public void testFactories() {
        assertThat(Heylogs.builder().build())
                .returns(0, heylogs -> heylogs.getRules().size())
                .returns(0, heylogs -> heylogs.getFormats().size());

        assertThat(Heylogs.ofServiceLoader())
                .extracting(Heylogs::getRules, list(Rule.class))
                .hasSizeGreaterThan(1)
                .map(Rule::getRuleId)
                .doesNotContain("semver");

        assertThat(Heylogs.ofServiceLoader().toBuilder().rule(new MockedRule()).build())
                .extracting(Heylogs::getRules, list(Rule.class))
                .hasSizeGreaterThan(1)
                .map(Rule::getRuleId)
                .contains("mocked");
    }

    @Test
    public void testCheck() {
        Heylogs api = Heylogs.ofServiceLoader();
        Heylogs empty = Heylogs.builder().build();

        assertThat(api.check(using("/Main.md"), Config.DEFAULT)).isEmpty();
        assertThat(empty.check(using("/Main.md"), Config.DEFAULT)).isEmpty();

        assertThat(api.check(using("/InvalidVersion.md"), Config.DEFAULT)).isNotEmpty();
        assertThat(empty.check(using("/InvalidVersion.md"), Config.DEFAULT)).isEmpty();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> api.check(using("/Main.md"), Config.builder().versioningOf("boom").build()))
                .withMessage("Cannot find versioning with id 'boom'");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> api.check(using("/Main.md"), Config.builder().versioningOf("regex").build()))
                .withMessageContaining("Invalid versioning argument 'null': ");

        assertThat(api.check(using("/Main.md"), Config.builder().versioningOf("regex:abc").build())).isNotEmpty();

        assertThat(api.check(using("/Main.md"), Config.builder().versioningOf("regex:abc").ruleOf("versioning-format:OFF").build())).isEmpty();

        assertThat(api.check(using("/Main.md"), Config.builder().versioningOf("regex:.*").build())).isEmpty();
    }

    @Test
    public void testExtract() {
        Heylogs x = Heylogs.ofServiceLoader();

        Function<Filter, String> usingMain = extractor -> extractToString(x, using("/Main.md"), extractor);

        assertThat(Filter.builder().ref("1.1.0").build())
                .extracting(usingMain, STRING)
                .isEqualTo(
                        "## [1.1.0] - 2019-02-15\n" +
                                "\n" +
                                "### Added\n" +
                                "\n" +
                                "- Danish translation from [@frederikspang](https://github.com/frederikspang).\n" +
                                "- Georgian translation from [@tatocaster](https://github.com/tatocaster).\n" +
                                "- Changelog inconsistency section in Bad Practices\n" +
                                "\n" +
                                "### Changed\n" +
                                "\n" +
                                "- Fixed typos in Italian translation from [@lorenzo-arena](https://github.com/lorenzo-arena).\n" +
                                "- Fixed typos in Indonesian translation from [@ekojs](https://github.com/ekojs).\n" +
                                "\n" +
                                "[1.1.0]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.0.0...v1.1.0\n");

        assertThat(Filter.builder().ref("1.1.0").ignoreContent(true).build())
                .extracting(usingMain, STRING)
                .isEqualTo(
                        "## [1.1.0] - 2019-02-15\n" +
                                "\n" +
                                "[1.1.0]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.0.0...v1.1.0\n");

        assertThat(Filter.builder().ref("zzz").build())
                .extracting(usingMain, STRING)
                .isEmpty();
    }

    @Test
    public void testList() {
        assertThat(Heylogs.builder().build().list())
                .isEmpty();

        assertThat(Heylogs.ofServiceLoader().list())
                .isNotEmpty();
    }

    @Test
    public void testRelease() {
        Heylogs x = Heylogs.ofServiceLoader()
                .toBuilder()
                .clearForges().forge(MOCKED_FORGE.toBuilder().knownHostPredicate(ignore -> true).build())
                .clearVersionings().versioning(BaseVersionings.REGEX_VERSIONING)
                .build();

        LocalDate date = LocalDate.of(2010, 1, 1);

        assertThatIllegalArgumentException().isThrownBy(() -> releaseToString(x, using("/Main.md"), Version.of("42", null, HYPHEN, date, false), "boom"))
                .withMessageContaining("Cannot find versioning with id 'boom'");

        assertThatIllegalArgumentException().isThrownBy(() -> releaseToString(x, using("/Main.md"), Version.of("boom", null, HYPHEN, date, false), "regex:\\d+"))
                .withMessageContaining("Invalid version 'boom' for versioning 'regex:\\d+'");

        assertThatCode(() -> releaseToString(x, using("/Main.md"), Version.of("boom", null, HYPHEN, date, false), null))
                .doesNotThrowAnyException();

        assertThatCode(() -> releaseToString(x, using("/Main.md"), Version.of("42", null, HYPHEN, date, false), "regex:\\d+"))
                .doesNotThrowAnyException();

        Version v123 = Version.of("1.2.3", null, HYPHEN, date, false);

        assertThatIllegalArgumentException().isThrownBy(() -> releaseToString(x, using("/Empty.md"), v123, null))
                .withMessageContaining("Invalid changelog");

        assertThat(releaseToString(x, using("/Main.md"), v123, null))
                .contains(
                        "## [Unreleased]",
                        "## [1.2.3] - 2010-01-01",
                        "[Unreleased]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.2.3...HEAD",
                        "[1.2.3]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.1.0...v1.2.3")
                .doesNotContain("[unreleased]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.1.0...HEAD")
                .endsWith("[0.0.1]: https://github.com/olivierlacan/keep-a-changelog/releases/tag/v0.0.1\n");

        assertThat(releaseToString(x, using("/UnreleasedChanges.md"), v123, null))
                .contains(
                        "## [Unreleased]",
                        "## [1.2.3] - 2010-01-01",
                        "[Unreleased]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.2.3...HEAD",
                        "[1.2.3]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.1.0...v1.2.3")
                .doesNotContain("[unreleased]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.1.0...HEAD")
                .endsWith("[1.1.0]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.0.0...v1.1.0\n");

        assertThat(releaseToString(x, using("/FirstRelease.md"), v123, null))
                .contains(
                        "## [Unreleased]",
                        "## [1.2.3] - 2010-01-01",
                        "[Unreleased]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.2.3...HEAD",
                        "[1.2.3]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.2.3...v1.2.3")
                .doesNotContain("[unreleased]: https://github.com/olivierlacan/keep-a-changelog/compare/HEAD...HEAD")
                .endsWith("[1.2.3]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.2.3...v1.2.3\n");
    }

    @Test
    public void testScan() {
        Heylogs x = Heylogs.ofServiceLoader()
                .toBuilder()
                .forge(MOCKED_FORGE.toBuilder().knownHostPredicate(ignore -> true).build())
                .build();

        assertThat(x.scan(using("/Empty.md")))
                .isEqualTo(Summary
                        .builder()
                        .valid(false)
                        .releaseCount(0)
                        .timeRange(TimeRange.ALL)
                        .unreleasedChanges(0)
                        .build()
                );

        assertThat(x.scan(using("/Main.md")))
                .isEqualTo(Summary
                                .builder()
                                .valid(true)
                                .releaseCount(13)
                                .timeRange(TimeRange.of(LocalDate.of(2014, 5, 31), LocalDate.of(2019, 2, 15)))
//                        .compatibility("Semantic Versioning")
                                .unreleasedChanges(2)
                                .forgeName("GitHub")
                                .forgeURL(urlOf("https://github.com/olivierlacan/keep-a-changelog"))
                                .build()
                );

        assertThat(x.scan(using("/InvalidSemver.md")))
                .isEqualTo(Summary
                        .builder()
                        .valid(true)
                        .releaseCount(2)
                        .timeRange(TimeRange.of(LocalDate.of(2019, 2, 15), LocalDate.of(2019, 2, 15)))
                        .unreleasedChanges(0)
                        .forgeName("GitHub")
                        .forgeURL(urlOf("https://github.com/olivierlacan/keep-a-changelog"))
                        .build()
                );

        assertThat(x.scan(using("/InvalidVersion.md")))
                .isEqualTo(Summary
                        .builder()
                        .valid(false)
                        .releaseCount(0)
                        .timeRange(TimeRange.ALL)
                        .unreleasedChanges(0)
                        .build()
                );

        assertThat(x.scan(using("/YankedRelease.md")))
                .isEqualTo(Summary
                        .builder()
                        .valid(true)
                        .releaseCount(3)
                        .yankedReleaseCount(1)
                        .timeRange(TimeRange.of(LocalDate.of(2017, 6, 1), LocalDate.of(2019, 2, 15)))
                        .unreleasedChanges(1)
                        .forgeName("GitHub")
                        .forgeURL(urlOf("https://github.com/example/project"))
                        .build()
                );
    }

    @Test
    public void testFormatProblems() throws IOException {
        List<Check> checks = singletonList(Check.builder().source("file1").problem(Problem.builder().id("rule1").severity(ERROR).issue(RuleIssue.builder().message("some message").line(10).column(20).build()).build()).build());

        assertThatIOException()
                .isThrownBy(() -> Heylogs.builder().build().formatProblems(FIRST_FORMAT_AVAILABLE, new StringBuilder(), checks));

        assertThatIOException()
                .isThrownBy(() -> Heylogs.ofServiceLoader().formatProblems("other", new StringBuilder(), checks));

        StringBuilder output = new StringBuilder();
        Heylogs.ofServiceLoader().formatProblems(StylishFormat.ID, output, checks);
        assertThat(output.toString())
                .isEqualToIgnoringNewLines(
                        "file1\n" +
                                "  10:20  error  some message  rule1\n" +
                                "  \n" +
                                "  1 problem\n"
                );
    }

    @Test
    public void testFormatStatus() throws IOException {
        List<Scan> scans = singletonList(
                Scan
                        .builder()
                        .source("file1")
                        .summary(
                                Summary
                                        .builder()
                                        .valid(true)
                                        .releaseCount(1)
                                        .timeRange(TimeRange.of(LocalDate.of(2019, 2, 15), LocalDate.of(2019, 2, 15)))
                                        .compatibility("Semantic Versioning")
                                        .unreleasedChanges(3)
                                        .forgeName("GitStuff")
                                        .forgeURL(urlOf("https://localhost:8080/hello"))
                                        .build())
                        .build());

        assertThatIOException()
                .isThrownBy(() -> Heylogs.builder().build().formatStatus(FIRST_FORMAT_AVAILABLE, new StringBuilder(), scans));

        assertThatIOException()
                .isThrownBy(() -> Heylogs.ofServiceLoader().formatStatus("other", new StringBuilder(), scans));

        StringBuilder output = new StringBuilder();
        Heylogs.ofServiceLoader().formatStatus(StylishFormat.ID, output, scans);
        assertThat(output.toString())
                .isEqualToIgnoringNewLines(
                        "file1\n" +
                                "  Valid changelog                                     \n" +
                                "  Found 1 releases                                    \n" +
                                "  Ranging from 2019-02-15 to 2019-02-15               \n" +
                                "  Compatible with Semantic Versioning                 \n" +
                                "  Forged with GitStuff at https://localhost:8080/hello\n" +
                                "  Has 3 unreleased changes                            \n"
                );
    }

    @Test
    public void testCheckConfig() {
        Heylogs x = Heylogs.ofServiceLoader()
                .toBuilder()
                .forge(MOCKED_FORGE)
                .build();

        assertThat(x.getForges()).isNotEmpty();
        assertThat(x.getTaggings()).isNotEmpty();
        assertThat(x.getVersionings()).isNotEmpty();
        assertThat(x.getFormats()).isNotEmpty();
        assertThat(x.getRules()).isNotEmpty();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.checkConfig(Config.builder().versioningOf("boom").build()))
                .withMessageContaining("Cannot find versioning with id 'boom'");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.checkConfig(Config.builder().versioningOf("regex").build()))
                .withMessageContaining("Invalid versioning argument 'null': ");
        assertThatCode(() -> x.checkConfig(Config.builder().versioningOf("regex:.*").build()))
                .doesNotThrowAnyException();

        assertThatIllegalArgumentException().isThrownBy(() -> x.checkConfig(Config.builder().taggingOf("boom").build()))
                .withMessageContaining("Cannot find tagging with id 'boom'");
        assertThatIllegalArgumentException().isThrownBy(() -> x.checkConfig(Config.builder().taggingOf("prefix:").build()))
                .withMessageContaining("Invalid tagging argument '': Prefix cannot be null or empty");
        assertThatCode(() -> x.checkConfig(Config.builder().taggingOf("prefix:abc").build()))
                .doesNotThrowAnyException();

        assertThatIllegalArgumentException().isThrownBy(() -> x.checkConfig(Config.builder().ruleOf("boom").build()))
                .withMessageContaining("Cannot find rule with id 'boom'");
        assertThatCode(() -> x.checkConfig(Config.builder().ruleOf("https").build()))
                .doesNotThrowAnyException();

        assertThatIllegalArgumentException().isThrownBy(() -> x.checkConfig(Config.builder().domainOf("example:boom").build()))
                .withMessageContaining("Cannot find forge with id 'boom'");
        assertThatCode(() -> x.checkConfig(Config.builder().domainOf("example:github").build()))
                .doesNotThrowAnyException();
    }

    @Test
    public void testPush() {
        Heylogs x = Heylogs.ofServiceLoader();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.push(using("/Empty.md"), TypeOfChange.ADDED, "some message"))
                .withMessageContaining("Cannot locate changelog header");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.push(using("/Main.md"), TypeOfChange.ADDED, ""))
                .withMessageContaining("Message must not be empty");

        // Push to existing type-of-change group
        assertThat(pushToString(x, using("/UnreleasedChanges.md"), TypeOfChange.ADDED, "New feature"))
                .contains("### Added")
                .contains("- New feature");

        // Push to non-existing type-of-change group
        assertThat(pushToString(x, using("/UnreleasedChanges.md"), TypeOfChange.SECURITY, "Fix vulnerability"))
                .contains("### Security")
                .contains("- Fix vulnerability");

        // Push message with markdown links
        String msg = "Add check on GitHub Pull Request links [#173](https://github.com/nbbrd/heylogs/issues/173)";
        assertThat(pushToString(x, using("/UnreleasedChanges.md"), TypeOfChange.ADDED, msg))
                .contains("- " + msg);

        // Push to empty unreleased section
        assertThat(pushToString(x, using("/FirstRelease.md"), TypeOfChange.ADDED, "First change"))
                .contains("### Added")
                .contains("- First change");
    }

    @Test
    public void testYank() {
        Heylogs x = Heylogs.ofServiceLoader();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.yank(using("/Empty.md"), "1.0.0"))
                .withMessageContaining("Cannot locate changelog header");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.yank(using("/Main.md"), ""))
                .withMessageContaining("Ref must not be empty");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.yank(using("/Main.md"), "9.9.9"))
                .withMessageContaining("Cannot locate version '9.9.9'");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.yank(using("/Main.md"), "unreleased"))
                .withMessageContaining("Cannot yank unreleased version");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.yank(using("/YankedRelease.md"), "1.0.0"))
                .withMessageContaining("Version '1.0.0' is already yanked");

        // Yank an existing released version
        assertThat(yankToString(x, using("/YankedRelease.md"), "1.1.0"))
                .contains("## [1.1.0] - 2019-02-15 [YANKED]")
                .doesNotContain("## [1.1.0] - 2019-02-15\n");
    }

    @Test
    public void testFetch() {
        // No forge found for unknown host URL
        Heylogs noForge = Heylogs.builder().build();
        assertThatIOException()
                .isThrownBy(() -> noForge.fetch(using("/UnreleasedChanges.md"), TypeOfChange.ADDED, "https://unknown.example.com/issues/1"))
                .withMessageContaining("No forge found for URL");

        // Forge found but no message fetcher (URL input)
        Heylogs noFetcher = Heylogs.builder()
                .forge(ForgeSupport.builder()
                        .id("test")
                        .name("Test Forge")
                        .moduleId("test")
                        .knownHostPredicate(url -> true)
                        .build())
                .build();
        assertThatIOException()
                .isThrownBy(() -> noFetcher.fetch(using("/UnreleasedChanges.md"), TypeOfChange.ADDED, "https://example.com/issues/1"))
                .withMessageContaining("Cannot resolve url");

        // No forge in changelog for ref resolution
        assertThatIOException()
                .isThrownBy(() -> noForge.fetch(using("/UnreleasedChanges.md"), TypeOfChange.ADDED, "#1"))
                .withMessageContaining("Cannot determine forge from changelog");
    }

    @Test
    public void testInit() {
        Heylogs x = Heylogs.ofServiceLoader()
                .toBuilder()
                .clearVersionings().versioning(BaseVersionings.REGEX_VERSIONING)
                .build();

        // no versioning: description line has no versioning reference
        String defaultResult = unchecked(FlexmarkIO.newTextFormatter()::formatToString).apply(x.init(Config.DEFAULT, null, null));
        assertThat(defaultResult)
                .contains("# Changelog")
                .contains("## [Unreleased]")
                .contains("[Keep a Changelog]")
                .doesNotContain("adheres to");

        // with versioning: name and URL come from the service
        String versioningResult = unchecked(FlexmarkIO.newTextFormatter()::formatToString)
                .apply(x.init(Config.builder().versioningOf("regex:.*").build(), null, null));
        assertThat(versioningResult)
                .contains("adheres to")
                .contains("[Regex Versioning](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html)")
                .doesNotContain("Semantic Versioning");

        // custom template — all config variables
        Heylogs xFull = x.toBuilder().forge(MOCKED_FORGE).build();
        String customTemplate = "{{#versioning}}versioning:{{id}}:{{name}}{{/versioning}}" +
                "{{#tagging}} tagging:{{id}}:{{arg}}{{/tagging}}" +
                "{{#forge}} forge:{{id}}{{/forge}}" +
                "{{#rules}} rule:{{id}}:{{severity}}{{/rules}}" +
                "{{#domains}} domain:{{domain}}:{{forgeId}}{{/domains}}";
        Config fullConfig = Config.builder()
                .versioningOf("regex:.*")
                .taggingOf("prefix:v")
                .forgeOf("github")
                .ruleOf("no-empty-group:WARN")
                .domainOf("example.com:github")
                .build();
        String customResult = unchecked(FlexmarkIO.newTextFormatter()::formatToString)
                .apply(xFull.init(fullConfig, customTemplate, null));
        assertThat(customResult)
                .contains("versioning:regex:Regex Versioning")
                .contains("tagging:prefix:v")
                .contains("forge:github")
                .contains("rule:no-empty-group:WARN")
                .contains("domain:example.com:github");

        assertThat(x.check(x.init(Config.DEFAULT, null, null), Config.DEFAULT))
                .extracting(Problem::getId)
                .doesNotContain("changelog-heading", "version-format");
    }

    @Test
    public void testNote() {
        Heylogs heylogs = Heylogs.ofServiceLoader();

        // 1. Insert summary when none exists (FirstRelease.md)
        Document doc1 = using("/FirstRelease.md");
        heylogs.note(doc1, "This is a summary.");
        String result1 = unchecked(FlexmarkIO.newTextFormatter()::formatToString).apply(doc1);
        assertThat(result1)
                .contains("## [Unreleased]", "This is a summary.")
                .contains("[unreleased]: https://github.com/olivierlacan/keep-a-changelog/compare/HEAD...HEAD");

        // 2. Replace existing summary (UnreleasedChanges.md)
        Document doc2 = using("/UnreleasedChanges.md");
        heylogs.note(doc2, "Replaced summary");
        String result2 = unchecked(FlexmarkIO.newTextFormatter()::formatToString).apply(doc2);
        assertThat(result2)
                .contains("## [Unreleased]", "Replaced summary")
                .doesNotContain("It's a trap !")
                .contains("### Added", "### Fixed", "## [1.1.0] - 2019-02-15");

        // 3. Do not insert summary if empty
        Document doc3 = using("/FirstRelease.md");
        heylogs.note(doc3, "   ");
        String result3 = unchecked(FlexmarkIO.newTextFormatter()::formatToString).apply(doc3);
        assertThat(result3)
                .contains("## [Unreleased]")
                .doesNotContain("This is a summary.");

        // 4. Multi-line/markdown summary
        Document doc4 = using("/FirstRelease.md");
        String multiSummary = "This is a summary.\n\n- Point 1\n- Point 2";
        heylogs.note(doc4, multiSummary);
        String result4 = unchecked(FlexmarkIO.newTextFormatter()::formatToString).apply(doc4);
        assertThat(result4)
                .contains("This is a summary.", "- Point 1", "- Point 2");

        // 5. Unreleased as last node (should still insert summary)
        Document doc5 = using("/FirstRelease.md");
        heylogs.note(doc5, "End summary");
        String result5 = unchecked(FlexmarkIO.newTextFormatter()::formatToString).apply(doc5);
        assertThat(result5)
                .contains("End summary");
    }

    private static String pushToString(Heylogs heylogs, Document doc, TypeOfChange typeOfChange, String message) {
        heylogs.push(doc, typeOfChange, message);
        return unchecked(FlexmarkIO.newTextFormatter()::formatToString).apply(doc);
    }

    private static String yankToString(Heylogs heylogs, Document doc, String ref) {
        heylogs.yank(doc, ref);
        return unchecked(FlexmarkIO.newTextFormatter()::formatToString).apply(doc);
    }

    private static String extractToString(Heylogs heylogs, Document doc, Filter extractor) {
        heylogs.extract(doc, extractor);
        return unchecked(FlexmarkIO.newTextFormatter()::formatToString).apply(doc);
    }

    private static String releaseToString(Heylogs heylogs, Document doc, Version version, CharSequence versioning) {
        heylogs.release(doc, version, Config.builder().tagging(TaggingConfig.of("prefix", "v")).versioningOf(versioning).build());
        return unchecked(FlexmarkIO.newTextFormatter()::formatToString).apply(doc);
    }

    private static final ForgeSupport MOCKED_FORGE = ForgeSupport
            .builder()
            .id("github")
            .name("GitHub")
            .moduleId("github")
            .linkParser(ForgeLinkType.COMPARE, MockedCompareLink::new)
            .knownHostPredicate(url -> false)
            .build();

    private static final class MockedRule implements Rule {

        @Override
        public @NonNull String getRuleId() {
            return "mocked";
        }

        @Override
        public @NonNull String getRuleName() {
            return "";
        }

        @Override
        public @NonNull String getRuleModuleId() {
            return "mocked";
        }

        @Override
        public boolean isRuleAvailable() {
            return false;
        }

        @Override
        public @NonNull RuleSeverity getRuleSeverity() {
            return RuleSeverity.OFF;
        }

        @Override
        public @Nullable RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull RuleContext context) {
            return null;
        }
    }
}
