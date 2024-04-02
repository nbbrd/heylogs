package nbbrd.heylogs;

import lombok.NonNull;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleIssue;
import nbbrd.heylogs.spi.RuleSeverity;

import java.util.List;

@lombok.Value
@lombok.Builder
public class Failure {

    @lombok.NonNull
    String id;

    @lombok.NonNull
    RuleSeverity severity;

    @lombok.NonNull
    RuleIssue issue;

    public static boolean hasErrors(@NonNull List<Failure> failures) {
        return failures.stream().anyMatch(failure -> failure.getSeverity().equals(RuleSeverity.ERROR));
    }

    public static final class Builder {

        public @NonNull Builder rule(@NonNull Rule rule) {
            return id(rule.getRuleId())
                    .severity(rule.getRuleSeverity());
        }
    }
}
