package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import org.jspecify.annotations.Nullable;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

@lombok.Builder
public final class VersioningSupport implements Versioning {

    private final @NonNull String id;

    private final @NonNull String name;

    private final @NonNull String moduleId;

    private final @NonNull Function<@Nullable String, @Nullable String> validator;

    private final @NonNull Function<@Nullable String, @Nullable Predicate<@NonNull CharSequence>> predicate;

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
    public @NonNull Validator<String> getVersioningArgValidator() {
        return validator::apply;
    }

    @Override
    public @Nullable Predicate<CharSequence> getVersioningPredicateOrNull(@Nullable String arg) throws IllegalArgumentException {
        String error = validator.apply(arg);
        if (error != null) throw new IllegalArgumentException(error);
        return predicate.apply(arg);
    }

    @StaticFactoryMethod(Function.class)
    public static Function<@Nullable String, Predicate<@NonNull CharSequence>> withoutArg(@NonNull Predicate<CharSequence> predicate) {
        return arg -> arg == null ? predicate : null;
    }

    @StaticFactoryMethod(Function.class)
    public static <X> Function<@Nullable String, Predicate<@NonNull CharSequence>> compilingArg(@NonNull Function<String, X> factory, @NonNull BiPredicate<X, CharSequence> predicate) {
        return arg -> {
            X engine = factory.apply(arg);
            return text -> predicate.test(engine, text);
        };
    }
}
