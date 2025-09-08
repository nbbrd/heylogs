package nbbrd.heylogs;

import nbbrd.heylogs.spi.RuleIssue;
import nbbrd.heylogs.spi.RuleSeverity;

@lombok.Value
@lombok.Builder
public class Problem {

    @lombok.NonNull
    String id;

    @lombok.NonNull
    RuleSeverity severity;

    @lombok.NonNull
    RuleIssue issue;
}
