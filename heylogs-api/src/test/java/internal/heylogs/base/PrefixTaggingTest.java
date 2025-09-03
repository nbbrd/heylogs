package internal.heylogs.base;

import nbbrd.heylogs.spi.Tagging;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.heylogs.spi.TaggingAssert.assertTaggingCompliance;

class PrefixTaggingTest {

    @Test
    public void testCompliance() {
        assertTaggingCompliance(new PrefixTagging());
    }

    @Test
    public void testFormatter() {
        Tagging x = new PrefixTagging();

        assertThat(x.getTagFormatterOrNull(null))
                .isNull();

        assertThat(x.getTagFormatterOrNull("").apply("1.0.0"))
                .isEqualTo("1.0.0");

        assertThat(x.getTagFormatterOrNull("v").apply("1.0.0"))
                .isEqualTo("v1.0.0");
    }

    @Test
    public void testParser() {
        Tagging x = new PrefixTagging();

        assertThat(x.getTagParserOrNull(null))
                .isNull();

        assertThat(x.getTagParserOrNull("").apply("1.0.0"))
                .isEqualTo("1.0.0");

        assertThat(x.getTagParserOrNull("v").apply("v1.0.0"))
                .isEqualTo("1.0.0");

        assertThat(x.getTagParserOrNull("v").apply("1.0.0"))
                .isNull();
    }
}