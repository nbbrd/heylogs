package tests.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.spi.Forge;

import static internal.heylogs.URLExtractor.urlOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public final class ForgeAssert {

    private ForgeAssert() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @SuppressWarnings("DataFlowIssue")
    public static void assertForgeCompliance(@NonNull Forge x) {
        assertThat(x.getForgeId())
                .matches(nbbrd.heylogs.spi.ForgeLoader.ID_PATTERN);

        assertThat(x.getForgeName())
                .isNotEmpty()
                .isNotNull();

        assertThatNullPointerException()
                .isThrownBy(() -> x.isCompareLink(null));

        assertThatNullPointerException()
                .isThrownBy(() -> x.getProjectURL(null));

        assertThatNullPointerException()
                .isThrownBy(() -> x.deriveCompareLink(null, ""));

        assertThatNullPointerException()
                .isThrownBy(() -> x.deriveCompareLink(urlOf("http://localhost"), null));
    }
}
