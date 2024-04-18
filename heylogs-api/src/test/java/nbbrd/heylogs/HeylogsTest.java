package nbbrd.heylogs;

import internal.heylogs.StylishFormat;
import internal.heylogs.semver.SemVerRule;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleIssue;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static _test.Sample.using;
import static internal.heylogs.URLExtractor.urlOf;
import static java.util.Collections.singletonList;
import static nbbrd.heylogs.Heylogs.FIRST_FORMAT_AVAILABLE;
import static nbbrd.heylogs.spi.RuleSeverity.ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;
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
    public void testValidate() {
        assertThat(Heylogs.builder().build().validate(using("/InvalidVersion.md")))
                .isEmpty();

        assertThat(Heylogs.ofServiceLoader().validate(using("/InvalidVersion.md")))
                .isNotEmpty();
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
    void testScan() {
        Heylogs x = Heylogs.ofServiceLoader();

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
                        .compatibility("Semantic Versioning")
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
}
