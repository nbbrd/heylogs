package tests.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.spi.ForgeRef;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public final class ForgeRefAssert {

    private ForgeRefAssert() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @SuppressWarnings("DataFlowIssue")
    public static void assertForgeRefCompliance(@NonNull ForgeRef<?> x) {
        assertThatNullPointerException()
                .isThrownBy(() -> x.isCompatibleWith(null));

        assertThat(x.getClass())
                .isFinal();
    }
}
