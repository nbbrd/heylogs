package internal.heylogs;

import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.FormatLoader;
import org.junit.jupiter.api.Test;

import static tests.heylogs.api.Sample.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class JsonFormatTest {

    @Test
    public void testIdPattern() {
        assertThat(new JsonFormat().getFormatId())
                .matches(FormatLoader.ID_PATTERN);
    }

    @Test
    public void testFormatProblems() {
        Format x = new JsonFormat();

        assertThat(writing(appendable -> x.formatProblems(appendable, singletonList(CHECK1))))
                .isEqualToNormalizingNewlines(contentOf(JsonFormatTest.class, "check1.json"));

        assertThat(writing(appendable -> x.formatProblems(appendable, singletonList(CHECK2))))
                .isEqualToNormalizingNewlines(contentOf(JsonFormatTest.class, "check2.json"));

        assertThat(writing(appendable -> x.formatProblems(appendable, singletonList(CHECK3))))
                .isEqualToNormalizingNewlines(contentOf(JsonFormatTest.class, "check3.json"));
    }

    @Test
    public void testFormatStatus() {
        Format x = new JsonFormat();

        assertThat(writing(appendable -> x.formatStatus(appendable, singletonList(SCAN1))))
                .isEqualToNormalizingNewlines(contentOf(JsonFormatTest.class, "scan1.json"));

        assertThat(writing(appendable -> x.formatStatus(appendable, singletonList(SCAN2))))
                .isEqualToNormalizingNewlines(contentOf(JsonFormatTest.class, "scan2.json"));
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