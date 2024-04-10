package nbbrd.heylogs.spi;

import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;

@lombok.Value
@lombok.Builder
public class RuleIssue {

    @lombok.NonNull
    String message;

    int line;

    int column;

    public static final class Builder {

        public @NonNull Builder location(@NonNull Node location) {
            return line(location.getStartLineNumber() + 1)
                    .column(location.lineColumnAtStart().getSecond() + 1);
        }
    }
}
