package nbbrd.heylogs;

import lombok.NonNull;
import nbbrd.heylogs.spi.RuleSeverity;

import java.util.List;

@lombok.Value
@lombok.Builder
public class Check {

    @NonNull String source;

    @lombok.Singular
    List<Problem> problems;

    public boolean hasErrors() {
        return problems.stream().anyMatch(problem -> problem.getSeverity().equals(RuleSeverity.ERROR));
    }
}
