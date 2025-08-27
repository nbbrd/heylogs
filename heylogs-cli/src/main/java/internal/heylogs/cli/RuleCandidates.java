package internal.heylogs.cli;

import lombok.NonNull;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleLoader;

import java.util.Iterator;

public final class RuleCandidates implements Iterable<String> {

    @Override
    public @NonNull Iterator<String> iterator() {
        return RuleLoader.load()
                .stream()
                .map(Rule::getRuleId)
                .iterator();
    }
}
