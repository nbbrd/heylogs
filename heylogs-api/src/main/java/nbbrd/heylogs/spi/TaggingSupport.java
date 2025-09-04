package nbbrd.heylogs.spi;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;

@lombok.Builder
public final class TaggingSupport implements Tagging {

    private final @NonNull String id;

    private final @NonNull String name;

    private final @NonNull String moduleId;

    private final @NonNull Function<@Nullable String, @Nullable String> validator;

    private final @Nullable BiFunction<@Nullable String, @NonNull String, @NonNull String> formatter;

    private final @Nullable BiFunction<@Nullable String, @NonNull String, @NonNull String> parser;

    @Override
    public @NonNull String getTaggingId() {
        return id;
    }

    @Override
    public @NonNull String getTaggingName() {
        return name;
    }

    @Override
    public @NonNull String getTaggingModuleId() {
        return moduleId;
    }

    @Override
    public @NonNull Validator<String> getTaggingArgValidator() {
        return validator::apply;
    }

    @Override
    public @Nullable Converter<String, String> getTagFormatterOrNull(@Nullable String arg) throws IllegalArgumentException {
        String error = validator.apply(arg);
        if (error != null) throw new IllegalArgumentException(error);
        return formatter != null ? versionRef -> formatter.apply(arg, versionRef) : CONVERSION_NOT_SUPPORTED;
    }

    @Override
    public @Nullable Converter<String, String> getTagParserOrNull(@Nullable String arg) throws IllegalArgumentException {
        String error = validator.apply(arg);
        if (error != null) throw new IllegalArgumentException(error);
        return parser != null ? tag -> parser.apply(arg, tag) : CONVERSION_NOT_SUPPORTED;
    }
}
