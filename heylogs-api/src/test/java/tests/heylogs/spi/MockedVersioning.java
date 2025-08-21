package tests.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.spi.Versioning;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class MockedVersioning implements Versioning {

    @NonNull
    @lombok.Builder.Default
    String versioningId = "mocked-versioning";

    @NonNull
    @lombok.Builder.Default
    String versioningName = "Mocked Versioning";

    @NonNull
    @lombok.Builder.Default
    String versioningModuleId = "heylogs-api";

    @NonNull
    Function<@Nullable String, Predicate<@NonNull CharSequence>> validation;

    @Override
    public @NonNull Predicate<CharSequence> getVersioningPredicate(@Nullable String arg) {
        return validation.apply(arg);
    }

    public static boolean isInteger(@NonNull CharSequence text) {
        try {
            Integer.parseInt(text.toString());
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public static Predicate<CharSequence> ignoreVersioning() {
        return text -> {
            Objects.requireNonNull(text);
            return false;
        };
    }
}
