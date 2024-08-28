package tests.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.spi.Rule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public final class RuleAssert {

    private RuleAssert() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @SuppressWarnings("DataFlowIssue")
    public static void assertRuleCompliance(@NonNull Rule x) {
        assertThat(x.getRuleId())
                .matches(nbbrd.heylogs.spi.RuleLoader.ID_PATTERN);

        assertThat(x.getRuleName())
                .isNotEmpty()
                .isNotNull();

        assertThat(x.getRuleCategory())
                .isNotEmpty()
                .isNotNull();

        assertThat(x.getRuleSeverity())
                .isNotNull();

        assertThatNullPointerException()
                .isThrownBy(() -> x.getRuleIssueOrNull(null));

        assertThat(x.getClass())
                .isFinal();
    }
}
