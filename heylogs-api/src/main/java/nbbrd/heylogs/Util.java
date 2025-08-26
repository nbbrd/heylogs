package nbbrd.heylogs;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.function.Function;

public final class Util {

    private Util() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static <X, Y> Function<X, @Nullable Y> illegalArgumentToNull(Function<X, Y> function) {
        return x -> {
            try {
                return function.apply(x);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        };
    }

    public static @NonNull String toUnicode(@NonNull Character c) {
        return String.format(Locale.ROOT, "\\u%04x", (int) c);
    }
}
