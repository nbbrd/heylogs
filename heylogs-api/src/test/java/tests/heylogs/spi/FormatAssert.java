package tests.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.spi.Format;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public final class FormatAssert {

    private FormatAssert() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @SuppressWarnings("DataFlowIssue")
    public static void assertFormatCompliance(@NonNull Format x) {
        assertThat(x.getFormatId())
                .matches(nbbrd.heylogs.spi.FormatLoader.ID_PATTERN);

        assertThat(x.getFormatName())
                .isNotEmpty()
                .isNotNull();

        assertThat(x.getFormatCategory())
                .isNotEmpty()
                .isNotNull();

        assertThat(x.getSupportedFormatTypes())
                .isNotNull();

        assertThatNullPointerException()
                .isThrownBy(() -> x.formatStatus(null, emptyList()));

        assertThatNullPointerException()
                .isThrownBy(() -> x.formatStatus(new StringBuilder(), null));

        assertThatNullPointerException()
                .isThrownBy(() -> x.formatResources(null, emptyList()));

        assertThatNullPointerException()
                .isThrownBy(() -> x.formatResources(new StringBuilder(), null));

        assertThatNullPointerException()
                .isThrownBy(() -> x.formatProblems(null, emptyList()));

        assertThatNullPointerException()
                .isThrownBy(() -> x.formatProblems(new StringBuilder(), null));

        assertThat(x.getClass())
                .isFinal();
    }
}
