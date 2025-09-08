package nbbrd.heylogs.spi;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public interface Converter<X, Y> extends Function<X, Y> {

    @Override
    @NonNull
    Y apply(@NonNull X x) throws IllegalArgumentException;

    default @Nullable Y applyOrNull(@NonNull X x) {
        try {
            return apply(x);
        } catch (IllegalArgumentException ignore) {
            return null;
        }
    }
}
