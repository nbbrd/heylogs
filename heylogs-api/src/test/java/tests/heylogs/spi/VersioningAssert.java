package tests.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.spi.Versioning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public final class VersioningAssert {

    private VersioningAssert() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @SuppressWarnings("DataFlowIssue")
    public static void assertVersioningCompliance(@NonNull Versioning x) {
        assertThat(x.getVersioningId())
                .matches(nbbrd.heylogs.spi.VersioningLoader.ID_PATTERN);

        assertThat(x.getVersioningName())
                .isNotEmpty()
                .isNotNull();

        assertThatNullPointerException()
                .isThrownBy(() -> x.isValidVersion(null));

        assertThat(x.getClass())
                .isFinal();
    }
}
