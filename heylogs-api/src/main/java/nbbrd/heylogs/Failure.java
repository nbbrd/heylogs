package nbbrd.heylogs;

import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;
import nbbrd.heylogs.spi.Rule;

@lombok.Value
@lombok.Builder
public class Failure {

    @lombok.NonNull
    String ruleId;

    @lombok.NonNull
    String message;

    int line;

    int column;

    public static final class Builder {

        public @NonNull Builder rule(@NonNull Rule rule) {
            return ruleId(rule.getId());
        }

        public @NonNull Builder location(@NonNull Node location) {
            return line(location.getStartLineNumber() + 1)
                    .column(location.lineColumnAtStart().getSecond() + 1);
        }
    }
}
