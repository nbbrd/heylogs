package internal.heylogs;

import nbbrd.design.MightBePromoted;
import nbbrd.heylogs.Failure;
import nbbrd.heylogs.Resource;
import nbbrd.heylogs.Status;
import nbbrd.heylogs.TimeRange;
import nbbrd.heylogs.spi.RuleSeverity;
import nbbrd.io.function.IOConsumer;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static nbbrd.heylogs.spi.RuleSeverity.ERROR;
import static org.assertj.core.api.Assertions.assertThat;

class StylishFormatTest {

    @Test
    public void testFormatFailures() {
        StylishFormat x = new StylishFormat();

        Failure f1 = Failure.builder().ruleId("rule1").ruleSeverity(ERROR).message("boom").line(5).column(18).build();
        Failure f2 = Failure.builder().ruleId("rule222").ruleSeverity(ERROR).message("hello world").line(35).column(2).build();

        assertThat(stringOf(appendable -> x.formatFailures(appendable, "source1", emptyList())))
                .isEqualToNormalizingNewlines(
                        "source1\n"
                                + "  \n"
                                + "  No problem\n"
                );

        assertThat(stringOf(appendable -> x.formatFailures(appendable, "source2", singletonList(f1))))
                .isEqualToNormalizingNewlines(
                        "source2\n"
                                + "  5:18  error  boom  rule1\n"
                                + "  \n"
                                + "  1 problem\n"
                );

        assertThat(stringOf(appendable -> x.formatFailures(appendable, "source3", asList(f1, f2))))
                .isEqualToNormalizingNewlines(
                        "source3\n"
                                + "   5:18  error  boom         rule1  \n"
                                + "  35:2   error  hello world  rule222\n"
                                + "  \n"
                                + "  2 problems\n"
                );
    }

    @Test
    public void testFormatStatus() {
        StylishFormat x = new StylishFormat();

        Status s1 = Status.builder().build();
        assertThat(stringOf(appendable -> x.formatStatus(appendable, "source1", s1)))
                .isEqualToNormalizingNewlines(
                        "source1\n"
                                + "  No release found         \n"
                                + "  Has no unreleased version\n"
                );

        Status s2 = Status
                .builder()
                .compatibleWithSemver(true)
                .releaseCount(3)
                .hasUnreleasedSection(true)
                .semverDetails("XXX")
                .timeRange(TimeRange.of(LocalDate.of(2010, 1, 1), LocalDate.of(2011, 1, 1)))
                .build();
        assertThat(stringOf(appendable -> x.formatStatus(appendable, "source2", s2)))
                .isEqualToNormalizingNewlines(
                        "source2\n"
                                + "  Found 3 releases                      \n"
                                + "  Ranging from 2010-01-01 to 2011-01-01 \n"
                                + "  Compatible with Semantic VersioningXXX\n"
                                + "  Has an unreleased version             \n"
                );
    }

    @Test
    public void testFormatResource() {
        StylishFormat x = new StylishFormat();

        Resource r1 = new Resource("a", "hello");
        Resource r2 = new Resource("world", "b");

        assertThat(stringOf(appendable -> x.formatResources(appendable, emptyList())))
                .isEqualToNormalizingNewlines(
                        "Resources\n"
                                + "  \n"
                                + "  No resource found\n"
                );

        assertThat(stringOf(appendable -> x.formatResources(appendable, singletonList(r1))))
                .isEqualToNormalizingNewlines(
                        "Resources\n"
                                + "  a  hello\n"
                                + "  \n"
                                + "  1 resource found\n"
                );

        assertThat(stringOf(appendable -> x.formatResources(appendable, asList(r1, r2))))
                .isEqualToNormalizingNewlines(
                        "Resources\n"
                                + "  a      hello\n"
                                + "  world  b    \n"
                                + "  \n"
                                + "  2 resources found\n"
                );
    }

    @MightBePromoted
    private static String stringOf(IOConsumer<? super Appendable> consumer) {
        StringBuilder result = new StringBuilder();
        consumer.asUnchecked().accept(result);
        return result.toString();
    }
}