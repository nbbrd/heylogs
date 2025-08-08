package tests.heylogs.spi;

import com.vladsch.flexmark.util.ast.Document;
import lombok.NonNull;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleBatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public final class RuleAssert {

    private RuleAssert() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void assertRuleCompliance(@NonNull RuleBatch x) {
        x.getProviders().forEach(RuleAssert::assertRuleCompliance);
    }

    @SuppressWarnings("DataFlowIssue")
    public static void assertRuleCompliance(@NonNull Rule x) {
        assertThat(x.getRuleId())
                .matches(nbbrd.heylogs.spi.RuleLoader.ID_PATTERN);

        assertThat(x.getRuleName())
                .isNotEmpty()
                .isNotNull();

        assertThat(x.getRuleModuleId())
                .isNotEmpty()
                .isNotNull();

        assertThat(x.getRuleSeverity())
                .isNotNull();

        assertThatNullPointerException()
                .isThrownBy(() -> x.getRuleIssueOrNull(null, Config.DEFAULT));

        assertThatNullPointerException()
                .isThrownBy(() -> x.getRuleIssueOrNull(Document.NULL, null));
    }
}
