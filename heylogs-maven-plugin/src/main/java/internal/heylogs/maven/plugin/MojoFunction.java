package internal.heylogs.maven.plugin;

import lombok.NonNull;
import org.apache.maven.plugin.MojoExecutionException;

import java.util.function.Function;

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
}
