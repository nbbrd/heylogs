package nbbrd.heylogs;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Failure failure = (Failure) o;
        return line == failure.line && column == failure.column && Objects.equals(ruleId, failure.ruleId) && Objects.equals(message, failure.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleId, message, line, column);
    }
}
