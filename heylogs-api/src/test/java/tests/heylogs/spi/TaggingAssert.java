package tests.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.spi.Tagging;

import static org.assertj.core.api.Assertions.*;

public final class TaggingAssert {

    private TaggingAssert() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void assertTaggingCompliance(@NonNull Tagging x) {
        assertThat(x.getTaggingId())
                .matches(nbbrd.heylogs.spi.TaggingLoader.ID_PATTERN);

        assertThat(x.getTaggingName())
                .isNotEmpty()
                .isNotNull();

        assertThat(x.getTaggingModuleId())
                .isNotEmpty()
                .isNotNull();

        assertThat(x.getTaggingArgValidator())
                .satisfies(validator -> {
                    assertThat(validator).isNotNull();
                    assertThatCode(() -> validator.apply(null)).doesNotThrowAnyException();
                    String error = validator.apply(null);
                    if (error == null) {
                        assertThatCode(() -> x.getTagFormatterOrNull(null)).doesNotThrowAnyException();
                        assertThatCode(() -> x.getTagParserOrNull(null)).doesNotThrowAnyException();
                    } else {
                        assertThatIllegalArgumentException()
                                .isThrownBy(() -> x.getTagFormatterOrNull(null))
                                .withMessageContaining(error);
                        assertThatIllegalArgumentException()
                                .isThrownBy(() -> x.getTagParserOrNull(null))
                                .withMessageContaining(error);
                    }
                });
    }
}
