package tests.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.spi.Versioning;

import static org.assertj.core.api.Assertions.*;

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

        assertThat(x.getVersioningModuleId())
                .isNotEmpty()
                .isNotNull();

        assertThat(x.getVersioningArgValidator())
                .satisfies(validator -> {
                    assertThat(validator).isNotNull();
                    assertThatCode(() -> validator.apply(null)).doesNotThrowAnyException();
                    String error = validator.apply(null);
                    if (error == null) {
                        assertThatCode(() -> x.getVersioningPredicateOrNull(null)).doesNotThrowAnyException();
                        assertThatNullPointerException()
                                .isThrownBy(() -> x.getVersioningPredicateOrNull(null).test(null));
                    } else {
                        assertThatIllegalArgumentException()
                                .isThrownBy(() -> x.getVersioningPredicateOrNull(null))
                                .withMessageContaining(error);
                    }
                });
    }
}
