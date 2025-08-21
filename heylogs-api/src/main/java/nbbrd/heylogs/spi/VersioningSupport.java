package nbbrd.heylogs.spi;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

@lombok.Builder
public final class VersioningSupport implements Versioning {

    private final @NonNull String id;

    private final @NonNull String name;

    private final @NonNull String moduleId;

    private final @NonNull Function<@Nullable String, Predicate<@NonNull CharSequence>> validation;

    @Override
    public @NonNull String getVersioningId() {
        return id;
    }

    @Override
    public @NonNull String getVersioningName() {
        return name;
    }

    @Override
    public @NonNull String getVersioningModuleId() {
        return moduleId;
    }

    @Override
    public @NonNull Predicate<CharSequence> getVersioningPredicate(@Nullable String arg) {
        return validation.apply(arg);
    }

    public static @NonNull Predicate<Versioning> onVersioningId(@NonNull String id) {
        return versioning -> versioning.getVersioningId().equals(id);
    }

    public static Predicate<CharSequence> noValidation() {
        return text -> {
            Objects.requireNonNull(text);
            return false;
        };
    }

    public static <X> Function<@Nullable String, Predicate<@NonNull CharSequence>> composing(Function<String, X> factory, BiPredicate<X, CharSequence> predicate) {
        return arg -> compose(factory, predicate, arg);
    }

    public static <X> Predicate<@NonNull CharSequence> compose(Function<String, X> factory, BiPredicate<X, CharSequence> predicate, String arg) {
        if (arg != null) {
            try {
                X engine = factory.apply(arg);
                return text -> predicate.test(engine, text);
            } catch (Exception ignore) {
                // Ignore exceptions and return no validation
            }
        }
        return VersioningSupport.noValidation();
    }
}
