package tests.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.spi.Tagging;

import static org.assertj.core.api.Assertions.assertThat;

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
    }
}
