package nbbrd.heylogs;

import lombok.NonNull;

import java.util.function.Function;

public final class Util {

    private Util() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static @NonNull <X, Y> Function<X, Y> illegalArgumentToNull(Function<X, Y> function) {
        return x -> {
            try {
                return function.apply(x);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        };
    }
}
