package nbbrd.heylogs;

import lombok.NonNull;
import nbbrd.heylogs.spi.Rule;
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

    public static final class Builder {

        public @NonNull Builder rule(@NonNull Rule rule) {
            return id(rule.getRuleId())
                    .severity(rule.getRuleSeverity());
        }
    }
}
