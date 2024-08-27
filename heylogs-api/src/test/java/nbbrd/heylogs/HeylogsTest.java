package nbbrd.heylogs;

import _test.Sample;
import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.StylishFormat;
import internal.heylogs.semver.SemVerRule;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleIssue;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

import static _test.Sample.using;
import static internal.heylogs.URLExtractor.urlOf;
import static java.util.Collections.singletonList;
import static nbbrd.heylogs.Filter.builder;
import static nbbrd.heylogs.Heylogs.FIRST_FORMAT_AVAILABLE;
import static nbbrd.heylogs.spi.RuleSeverity.ERROR;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

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

        assertThat(Heylogs.ofServiceLoader().toBuilder().rule(new SemVerRule()).build())
                .extracting(Heylogs::getRules, list(Rule.class))
                .hasSizeGreaterThan(1)
                .map(Rule::getRuleId)
                .contains("semver");
    }

    @Test
    public void testCheckFormat() {
        assertThat(Heylogs.builder().build().checkFormat(using("/InvalidVersion.md")))
                .isEmpty();

        assertThat(Heylogs.ofServiceLoader().checkFormat(using("/InvalidVersion.md")))
                .isNotEmpty();
    }

    @Test
    public void testExtractVersions() {
        Heylogs x = Heylogs.ofServiceLoader();

        Function<Filter, String> usingMain = extractor -> {
            Document doc = using("/Main.md");
            x.extractVersions(doc, extractor);
            return Sample.FORMATTER.render(doc);
        };

        assertThat(builder().ref("1.1.0").build())
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
                                "[1.1.0]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.0.0...v1.1.0\n" +
                                "\n");

        assertThat(builder().ref("1.1.0").ignoreContent(true).build())
                .extracting(usingMain, STRING)
                .isEqualTo(
                        "## [1.1.0] - 2019-02-15\n" +
                                "\n" +
                                "[1.1.0]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.0.0...v1.1.0\n" +
                                "\n");

        assertThat(builder().ref("zzz").build())
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
        Heylogs x = Heylogs.ofServiceLoader();
        Version v123 = Version.of("1.2.3", Version.HYPHEN, LocalDate.of(2010, 1, 1));

        assertThatIllegalArgumentException().isThrownBy(() -> releaseChangesToString(x, using("/Empty.md"), v123))
                .withMessageContaining("Invalid changelog");

        assertThat(releaseChangesToString(x, using("/Main.md"), v123))
                .contains(
                        "## [Unreleased]",
                        "## [1.2.3] - 2010-01-01",
                        "[Unreleased]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.2.3...HEAD",
                        "[1.2.3]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.1.0...v1.2.3")
                .doesNotContain("[unreleased]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.1.0...HEAD");

        assertThat(releaseChangesToString(x, using("/UnreleasedChanges.md"), v123))
                .contains(
                        "## [Unreleased]",
                        "## [1.2.3] - 2010-01-01",
                        "[Unreleased]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.2.3...HEAD",
                        "[1.2.3]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.1.0...v1.2.3")
                .doesNotContain("[unreleased]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.1.0...HEAD");

        assertThat(releaseChangesToString(x, using("/FirstRelease.md"), v123))
                .contains(
                        "## [Unreleased]",
                        "## [1.2.3] - 2010-01-01",
                        "[Unreleased]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.2.3...HEAD",
                        "[1.2.3]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.2.3...v1.2.3")
                .doesNotContain("[unreleased]: https://github.com/olivierlacan/keep-a-changelog/compare/HEAD...HEAD");
    }

    @Test
    public void testScanContent() {
        Heylogs x = Heylogs.ofServiceLoader();

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
                        .compatibility("Semantic Versioning")
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

    private static String releaseChangesToString(Heylogs heylogs, Document doc, Version version) {
        heylogs.releaseChanges(doc, version, "v");
        return Sample.FORMATTER.render(doc);
    }
}
