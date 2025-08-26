package nbbrd.heylogs.spi;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

@lombok.Builder
public final class VersioningSupport implements Versioning {

    private final @NonNull String id;

    private final @NonNull String name;

    private final @NonNull String moduleId;

    private final @NonNull Function<@Nullable String, @Nullable Predicate<@NonNull CharSequence>> validation;

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
    public @Nullable Predicate<CharSequence> getVersioningPredicateOrNull(@Nullable String arg) {
        return validation.apply(arg);
    }

    public static @NonNull Predicate<Versioning> onVersioningId(@NonNull String id) {
        return versioning -> versioning.getVersioningId().equals(id);
    }

    public static Function<@Nullable String, Predicate<@NonNull CharSequence>> withoutArg(Predicate<CharSequence> predicate) {
        return arg -> arg == null ? predicate : null;
    }

    public static <X> Function<@Nullable String, Predicate<@NonNull CharSequence>> compilingArg(Function<String, X> factory, BiPredicate<X, CharSequence> predicate) {
        return arg -> {
            if (arg != null) {
                try {
                    X engine = factory.apply(arg);
                    return text -> predicate.test(engine, text);
                } catch (Exception ignore) {
                    // Ignore exceptions and return null
                }
            }
            return null;
        };
    }
}
