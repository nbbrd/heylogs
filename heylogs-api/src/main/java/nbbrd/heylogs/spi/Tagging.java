package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE
)
public interface Tagging {

    @ServiceId(pattern = ServiceId.KEBAB_CASE)
    @NonNull
    String getTaggingId();

    @NonNull
    String getTaggingName();

    @NonNull
    String getTaggingModuleId();

    @Nullable
    Function<String, String> getTagFormatterOrNull(@Nullable String arg);

    @Nullable
    Function<String, String> getTagParserOrNull(@Nullable String arg);

    @StaticFactoryMethod(Predicate.class)
    static @NonNull Predicate<Tagging> onTaggingId(@NonNull String id) {
        return forge -> forge.getTaggingId().equals(id);
    }
}
