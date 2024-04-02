package nbbrd.heylogs;

import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleSeverity;

import java.util.List;

@lombok.Value
@lombok.Builder
public class Failure {

    @lombok.NonNull
    String ruleId;

    @lombok.NonNull
    RuleSeverity ruleSeverity;

    @lombok.NonNull
    String message;

    int line;

    int column;

    public static boolean hasErrors(@NonNull List<Failure> failures) {
        return failures.stream().anyMatch(failure -> failure.getRuleSeverity().equals(RuleSeverity.ERROR));
    }

    public static final class Builder {

        public @NonNull Builder rule(@NonNull Rule rule) {
            return ruleId(rule.getId())
                    .ruleSeverity(rule.getRuleSeverity());
        }

        public @NonNull Builder location(@NonNull Node location) {
            return line(location.getStartLineNumber() + 1)
                    .column(location.lineColumnAtStart().getSecond() + 1);
        }
    }
}
