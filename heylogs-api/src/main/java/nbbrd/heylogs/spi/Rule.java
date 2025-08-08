package nbbrd.heylogs.spi;

import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;
import nbbrd.heylogs.Config;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceFilter;
import nbbrd.service.ServiceId;
import org.jspecify.annotations.Nullable;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        batchType = RuleBatch.class
)
public interface Rule {

    @ServiceId(pattern = ServiceId.KEBAB_CASE)
    @NonNull
    String getRuleId();

    @NonNull
    String getRuleName();

    @NonNull
    String getRuleModuleId();

    @ServiceFilter
    boolean isRuleAvailable();

    @NonNull
    RuleSeverity getRuleSeverity();

    @Nullable
    RuleIssue getRuleIssueOrNull(@NonNull Node node, @NonNull Config config);

    RuleIssue NO_RULE_ISSUE = null;
}
