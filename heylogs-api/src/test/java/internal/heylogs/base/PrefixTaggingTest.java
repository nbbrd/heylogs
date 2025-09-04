package internal.heylogs.base;

import nbbrd.heylogs.spi.Tagging;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.TaggingAssert.assertTaggingCompliance;

class PrefixTaggingTest {

    @Test
    public void testCompliance() {
        assertTaggingCompliance(new PrefixTagging());
    }

    @Test
    public void testGetTaggingArgValidator() {
        Tagging x = new PrefixTagging();

        assertThat(x.getTaggingArgValidator())
                .isNotNull()
                .returns(null, v -> v.apply("v"))
                .returns("Prefix cannot be null or empty", v -> v.apply(null))
                .returns("Prefix cannot be null or empty", v -> v.apply(""));
    }

    @Test
    public void testGetTagFormatterOrNull() {
        Tagging x = new PrefixTagging();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getTagFormatterOrNull(null))
                .withMessage("Prefix cannot be null or empty");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getTagFormatterOrNull(""))
                .withMessage("Prefix cannot be null or empty");

        assertThat(x.getTagFormatterOrNull("v"))
                .satisfies(formatter -> {
                    assertThat(formatter).isNotNull();
                    assertThat(formatter.apply("1.0.0")).isEqualTo("v1.0.0");
                    assertThat(formatter.apply("Hello")).isEqualTo("vHello");
                });
    }

    @Test
    public void testGetTagParserOrNull() {
        Tagging x = new PrefixTagging();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getTagParserOrNull(null))
                .withMessage("Prefix cannot be null or empty");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getTagParserOrNull(""))
                .withMessage("Prefix cannot be null or empty");

        assertThat(x.getTagParserOrNull("v"))
                .satisfies(parser -> {
                    assertThat(parser).isNotNull();
                    assertThatIllegalArgumentException()
                            .isThrownBy(() -> parser.apply("1.0.0"))
                            .withMessage("Tag does not start with the specified prefix 'v'");
                    assertThat(parser.apply("v1.0.0"))
                            .isEqualTo("1.0.0");
                });
    }
}