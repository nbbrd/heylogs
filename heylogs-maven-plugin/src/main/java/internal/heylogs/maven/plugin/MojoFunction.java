package internal.heylogs.maven.plugin;

import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.Extractor;
import org.apache.maven.plugin.MojoExecutionException;

import java.time.LocalDate;
import java.util.function.Function;
import java.util.regex.Pattern;

@FunctionalInterface
public interface MojoFunction<X, Y> {

    Y applyWithMojo(X x) throws MojoExecutionException;

    static <X, Y> @NonNull MojoFunction<X, Y> of(@NonNull Function<X, Y> function, @NonNull String errorMessage) {
        return x -> {
            try {
                return function.apply(x);
            } catch (IllegalArgumentException ex) {
                throw new MojoExecutionException(errorMessage, ex);
            }
        };
    }

    @StaticFactoryMethod
    static @NonNull MojoFunction<String, Pattern> onPattern(@NonNull String errorMessage) {
        return of(Pattern::compile, errorMessage);
    }

    @StaticFactoryMethod
    static @NonNull MojoFunction<String, LocalDate> onLocalDate(@NonNull String errorMessage) {
        return of(Extractor::parseLocalDate, errorMessage);
    }
}
