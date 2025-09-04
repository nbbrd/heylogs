package nbbrd.heylogs.spi;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface Validator<T> extends Function<T, String> {

    @Override
    @Nullable
    String apply(@Nullable T t);

    static <T> @NonNull Validator<T> of(@NonNull Converter<T, ?> converter) {
        return t -> {
            try {
                converter.apply(Objects.requireNonNull(t));
                return null;
            } catch (IllegalArgumentException ex) {
                return "Value is invalid: " + ex.getMessage();
            } catch (NullPointerException ignore) {
                return "Value is null";
            }
        };
    }
}
