package nbbrd.heylogs;

import internal.heylogs.SemverRule;
import internal.heylogs.StylishFormat;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleIssue;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;

import static _test.Sample.using;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
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

        assertThat(Heylogs.ofServiceLoader().toBuilder().rule(new SemverRule()).build())
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
        assertThatIOException()
                .isThrownBy(() -> Heylogs.builder().build().formatProblems(FIRST_FORMAT_AVAILABLE, new StringBuilder(), "", emptyList()));

        assertThatIOException()
                .isThrownBy(() -> Heylogs.ofServiceLoader().formatProblems("other", new StringBuilder(), "", emptyList()));

        StringBuilder output = new StringBuilder();
        Heylogs.ofServiceLoader().formatProblems(StylishFormat.ID, output, "file1", asList(Problem.builder().id("rule1").severity(ERROR).issue(RuleIssue.builder().message("some message").line(10).column(20).build()).build()));
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
                .isEqualTo(new Status(
                        0,
                        TimeRange.ALL,
                        true, " ()",
                        true
                ));

        assertThat(x.scan(using("/Main.md")))
                .isEqualTo(new Status(
                        13,
                        TimeRange.of(LocalDate.of(2014, 5, 31), LocalDate.of(2019, 2, 15)),
                        true, " (1 MAJOR, 4 MINOR, 7 PATCH)",
                        true
                ));

        assertThat(x.scan(using("/InvalidSemver.md")))
                .isEqualTo(new Status(
                        2,
                        TimeRange.of(LocalDate.of(2019, 2, 15), LocalDate.of(2019, 2, 15)),
                        false, "",
                        true
                ));

        assertThat(x.scan(using("/InvalidVersion.md")))
                .isEqualTo(new Status(
                        1,
                        TimeRange.of(LocalDate.of(2019, 2, 15), LocalDate.of(2019, 2, 15)),
                        true, " ()",
                        true
                ));
    }

    @Test
    public void testFormatStatus() throws IOException {
        assertThatIOException()
                .isThrownBy(() -> Heylogs.builder().build().formatStatus(FIRST_FORMAT_AVAILABLE, new StringBuilder(), "", Status.builder().build()));

        assertThatIOException()
                .isThrownBy(() -> Heylogs.ofServiceLoader().formatStatus("other", new StringBuilder(), "", Status.builder().build()));

        StringBuilder output = new StringBuilder();
        Heylogs.ofServiceLoader().formatStatus(StylishFormat.ID, output, "file1", new Status(
                1,
                TimeRange.of(LocalDate.of(2019, 2, 15), LocalDate.of(2019, 2, 15)),
                true, " ()",
                true
        ));
        assertThat(output.toString())
                .isEqualToIgnoringNewLines(
                        "file1\n" +
                                "  Found 1 releases                      \n" +
                                "  Ranging from 2019-02-15 to 2019-02-15 \n" +
                                "  Compatible with Semantic Versioning ()\n" +
                                "  Has an unreleased version             \n"
                );
    }
}
