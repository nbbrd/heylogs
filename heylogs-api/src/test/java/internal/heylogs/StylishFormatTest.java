package internal.heylogs;

import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.FormatType;
import org.junit.jupiter.api.Test;

import static _test.Sample.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class StylishFormatTest {

    @Test
    public void testSupportedTypes() {
        Format x = new StylishFormat();

        assertThat(x.getSupportedFormatTypes())
                .containsExactlyInAnyOrder(FormatType.values());
    }

    @Test
    public void testFormatName() {
        Format x = new StylishFormat();

        assertThat(x.getFormatName()).isNotBlank();
    }

    @Test
    public void testFormatProblems() {
        Format x = new StylishFormat();

        assertThat(writing(appendable -> x.formatProblems(appendable, "source1", emptyList())))
                .isEqualToNormalizingNewlines(
                        "source1\n"
                                + "  \n"
                                + "  No problem\n"
                );

        assertThat(writing(appendable -> x.formatProblems(appendable, "source2", singletonList(PROBLEM1))))
                .isEqualToNormalizingNewlines(
                        "source2\n"
                                + "  5:18  error  boom  rule1\n"
                                + "  \n"
                                + "  1 problem\n"
                );

        assertThat(writing(appendable -> x.formatProblems(appendable, "source3", asList(PROBLEM1, PROBLEM2))))
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
        Format x = new StylishFormat();

        assertThat(writing(appendable -> x.formatStatus(appendable, "source1", STATUS1)))
                .isEqualToNormalizingNewlines(
                        "source1\n"
                                + "  No release found         \n"
                                + "  Has no unreleased version\n"
                );

        assertThat(writing(appendable -> x.formatStatus(appendable, "source2", STATUS2)))
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
        Format x = new StylishFormat();

        assertThat(writing(appendable -> x.formatResources(appendable, emptyList())))
                .isEqualToNormalizingNewlines(
                        "Resources\n"
                                + "  \n"
                                + "  No resource found\n"
                );

        assertThat(writing(appendable -> x.formatResources(appendable, singletonList(RESOURCE1))))
                .isEqualToNormalizingNewlines(
                        "Resources\n"
                                + "  a  hello\n"
                                + "  \n"
                                + "  1 resource found\n"
                );

        assertThat(writing(appendable -> x.formatResources(appendable, asList(RESOURCE1, RESOURCE2))))
                .isEqualToNormalizingNewlines(
                        "Resources\n"
                                + "  a      hello\n"
                                + "  world  b    \n"
                                + "  \n"
                                + "  2 resources found\n"
                );
    }
}