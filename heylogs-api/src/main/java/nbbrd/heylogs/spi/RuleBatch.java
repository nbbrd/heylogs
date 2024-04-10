package nbbrd.heylogs.spi;

import lombok.NonNull;

import java.util.stream.Stream;

public interface RuleBatch {

    @NonNull Stream<Rule> getProviders();
}
