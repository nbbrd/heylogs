package tests.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.FormatType;

import java.io.StringReader;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static tests.heylogs.api.Sample.CONTENT1;

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

        assertThat(x.getFormatModuleId())
                .isNotEmpty()
                .isNotNull();

        Set<FormatType> supported = x.getSupportedFormatTypes();
        assertThat(supported).isNotNull();

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

        if (!supported.contains(FormatType.PROBLEMS)) {
            assertThatThrownBy(() -> x.formatProblems(new StringBuilder(), emptyList()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        if (!supported.contains(FormatType.STATUS)) {
            assertThatThrownBy(() -> x.formatStatus(new StringBuilder(), emptyList()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        if (!supported.contains(FormatType.RESOURCES)) {
            assertThatThrownBy(() -> x.formatResources(new StringBuilder(), emptyList()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        if (!supported.contains(FormatType.CONTENT)) {
            assertThatThrownBy(() -> x.formatContent(new StringBuilder(), CONTENT1))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> x.parseContent(new StringReader("{}")))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
