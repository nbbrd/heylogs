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
import java.net.URL;
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
    public void testCheckFormat() {
        Heylogs api = Heylogs.ofServiceLoader();
        Heylogs empty = Heylogs.builder().build();

        assertThat(api.checkFormat(using("/Main.md"), Config.DEFAULT)).isEmpty();
        assertThat(empty.checkFormat(using("/Main.md"), Config.DEFAULT)).isEmpty();

        assertThat(api.checkFormat(using("/InvalidVersion.md"), Config.DEFAULT)).isNotEmpty();
        assertThat(empty.checkFormat(using("/InvalidVersion.md"), Config.DEFAULT)).isEmpty();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> api.checkFormat(using("/Main.md"), Config.builder().versioningOf("boom").build()))
                .withMessage("Cannot find versioning with id 'boom'");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> api.checkFormat(using("/Main.md"), Config.builder().versioningOf("regex").build()))
                .withMessageContaining("Invalid versioning argument 'null': ");

        assertThat(api.checkFormat(using("/Main.md"), Config.builder().versioningOf("regex:abc").build())).isNotEmpty();

        assertThat(api.checkFormat(using("/Main.md"), Config.builder().versioningOf("regex:abc").ruleOf("versioning-format:OFF").build())).isEmpty();

        assertThat(api.checkFormat(using("/Main.md"), Config.builder().versioningOf("regex:.*").build())).isEmpty();
    }

    @Test
    public void testExtractVersions() {
        Heylogs x = Heylogs.ofServiceLoader();

        Function<Filter, String> usingMain = extractor -> extractVersionsToString(x, using("/Main.md"), extractor);

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
    public void testListResources() {
        assertThat(Heylogs.builder().build().listResources())
                .isEmpty();

        assertThat(Heylogs.ofServiceLoader().listResources())
                .isNotEmpty();
    }

    @Test
    public void testReleaseChanges() {
        Heylogs x = Heylogs.ofServiceLoader()
                .toBuilder()
                .clearForges().forge(new MockedForge())
                .clearVersionings().versioning(BaseVersionings.REGEX_VERSIONING)
                .build();

        LocalDate date = LocalDate.of(2010, 1, 1);

        assertThatIllegalArgumentException().isThrownBy(() -> releaseChangesToString(x, using("/Main.md"), Version.of("42", null, HYPHEN, date), "boom"))
                .withMessageContaining("Cannot find versioning with id 'boom'");

        assertThatIllegalArgumentException().isThrownBy(() -> releaseChangesToString(x, using("/Main.md"), Version.of("boom", null, HYPHEN, date), "regex:\\d+"))
                .withMessageContaining("Invalid version 'boom' for versioning 'regex:\\d+'");

        assertThatCode(() -> releaseChangesToString(x, using("/Main.md"), Version.of("boom", null, HYPHEN, date), null))
                .doesNotThrowAnyException();

        assertThatCode(() -> releaseChangesToString(x, using("/Main.md"), Version.of("42", null, HYPHEN, date), "regex:\\d+"))
                .doesNotThrowAnyException();

        Version v123 = Version.of("1.2.3", null, HYPHEN, date);

        assertThatIllegalArgumentException().isThrownBy(() -> releaseChangesToString(x, using("/Empty.md"), v123, null))
                .withMessageContaining("Invalid changelog");

        assertThat(releaseChangesToString(x, using("/Main.md"), v123, null))
                .contains(
                        "## [Unreleased]",
                        "## [1.2.3] - 2010-01-01",
                        "[Unreleased]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.2.3...HEAD",
                        "[1.2.3]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.1.0...v1.2.3")
                .doesNotContain("[unreleased]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.1.0...HEAD")
                .endsWith("[0.0.1]: https://github.com/olivierlacan/keep-a-changelog/releases/tag/v0.0.1\n");

        assertThat(releaseChangesToString(x, using("/UnreleasedChanges.md"), v123, null))
                .contains(
                        "## [Unreleased]",
                        "## [1.2.3] - 2010-01-01",
                        "[Unreleased]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.2.3...HEAD",
                        "[1.2.3]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.1.0...v1.2.3")
                .doesNotContain("[unreleased]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.1.0...HEAD")
                .endsWith("[1.1.0]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.0.0...v1.1.0\n");

        assertThat(releaseChangesToString(x, using("/FirstRelease.md"), v123, null))
                .contains(
                        "## [Unreleased]",
                        "## [1.2.3] - 2010-01-01",
                        "[Unreleased]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.2.3...HEAD",
                        "[1.2.3]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.2.3...v1.2.3")
                .doesNotContain("[unreleased]: https://github.com/olivierlacan/keep-a-changelog/compare/HEAD...HEAD")
                .endsWith("[1.2.3]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.2.3...v1.2.3\n");
    }

    @Test
    public void testScanContent() {
        Heylogs x = Heylogs.ofServiceLoader()
                .toBuilder()
                .forge(new MockedForge())
                .build();

        assertThat(x.scanContent(using("/Empty.md")))
                .isEqualTo(Summary
                        .builder()
                        .valid(false)
                        .releaseCount(0)
                        .timeRange(TimeRange.ALL)
                        .unreleasedChanges(0)
                        .build()
                );

        assertThat(x.scanContent(using("/Main.md")))
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

        assertThat(x.scanContent(using("/InvalidSemver.md")))
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

        assertThat(x.scanContent(using("/InvalidVersion.md")))
                .isEqualTo(Summary
                        .builder()
                        .valid(false)
                        .releaseCount(0)
                        .timeRange(TimeRange.ALL)
                        .unreleasedChanges(0)
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
                .forge(new MockedForge())
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

    private static String extractVersionsToString(Heylogs heylogs, Document doc, Filter extractor) {
        heylogs.extractVersions(doc, extractor);
        return unchecked(FlexmarkIO.newTextFormatter()::formatToString).apply(doc);
    }

    private static String releaseChangesToString(Heylogs heylogs, Document doc, Version version, CharSequence versioning) {
        heylogs.releaseChanges(doc, version, Config.builder().tagging(TaggingConfig.of("prefix", "v")).versioningOf(versioning).build());
        return unchecked(FlexmarkIO.newTextFormatter()::formatToString).apply(doc);
    }

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

    private static final class MockedForge implements Forge {

        @Override
        public @NonNull String getForgeId() {
            return "github";
        }

        @Override
        public @NonNull String getForgeName() {
            return "GitHub";
        }

        @Override
        public @NonNull String getForgeModuleId() {
            return "github";
        }

        @Override
        public boolean isCompareLink(@NonNull URL url) {
            return true;
        }

        @Override
        public @NonNull CompareLink getCompareLink(@NonNull URL url) {
            return new MockedCompareLink(url);
        }

        @Override
        public @Nullable Function<? super URL, ForgeLink> getLinkParser(@NonNull ForgeRefType type) {
            return null;
        }

        @Override
        public @Nullable Function<? super CharSequence, ForgeRef> getRefParser(@NonNull ForgeRefType type) {
            return null;
        }

        @Override
        public boolean isKnownHost(@NonNull URL url) {
            return false;
        }
    }
}
