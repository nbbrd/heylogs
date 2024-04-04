package internal.heylogs;

import nbbrd.heylogs.spi.Format;
import org.junit.jupiter.api.Test;

import static _test.Sample.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class JsonFormatTest {

    @Test
    public void testFormatProblems() {
        Format x = new JsonFormat();

        assertThat(writing(appendable -> x.formatProblems(appendable, "source1", emptyList())))
                .isEqualToNormalizingNewlines(contentOf(JsonFormatTest.class, "problems1.json"));

        assertThat(writing(appendable -> x.formatProblems(appendable, "source2", singletonList(PROBLEM1))))
                .isEqualToNormalizingNewlines(contentOf(JsonFormatTest.class, "problems2.json"));

        assertThat(writing(appendable -> x.formatProblems(appendable, "source3", asList(PROBLEM1, PROBLEM2))))
                .isEqualToNormalizingNewlines(contentOf(JsonFormatTest.class, "problems3.json"));
    }

    @Test
    public void testFormatStatus() {
        Format x = new JsonFormat();

        assertThat(writing(appendable -> x.formatStatus(appendable, "source1", STATUS1)))
                .isEqualToNormalizingNewlines(contentOf(JsonFormatTest.class, "status1.json"));

        assertThat(writing(appendable -> x.formatStatus(appendable, "source2", STATUS2)))
                .isEqualToNormalizingNewlines(contentOf(JsonFormatTest.class, "status2.json"));
    }

    @Test
    public void testFormatResource() {
        Format x = new JsonFormat();

        assertThat(writing(appendable -> x.formatResources(appendable, emptyList())))
                .isEqualToNormalizingNewlines(contentOf(JsonFormatTest.class, "resource1.json"));

        assertThat(writing(appendable -> x.formatResources(appendable, singletonList(RESOURCE1))))
                .isEqualToNormalizingNewlines(contentOf(JsonFormatTest.class, "resource2.json"));

        assertThat(writing(appendable -> x.formatResources(appendable, asList(RESOURCE1, RESOURCE2))))
                .isEqualToNormalizingNewlines(contentOf(JsonFormatTest.class, "resource3.json"));
    }

}