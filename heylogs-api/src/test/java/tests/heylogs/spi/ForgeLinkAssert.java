package tests.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.spi.ForgeLink;

import static org.assertj.core.api.Assertions.assertThat;

public final class ForgeLinkAssert {

    private ForgeLinkAssert() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void assertForgeLinkCompliance(@NonNull ForgeLink x) {
        assertThat(x.getBase())
                .isNotNull();

        assertThat(x.getClass())
                .isFinal();
    }
}
