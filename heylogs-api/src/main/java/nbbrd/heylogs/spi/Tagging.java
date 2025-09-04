package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import org.jspecify.annotations.Nullable;

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

    @NonNull
    Validator<String> getTaggingArgValidator();

    @Nullable
    Converter<String, String> getTagFormatterOrNull(@Nullable String arg) throws IllegalArgumentException;

    @Nullable
    Converter<String, String> getTagParserOrNull(@Nullable String arg) throws IllegalArgumentException;

    Converter<String, String> CONVERSION_NOT_SUPPORTED = null;
}
