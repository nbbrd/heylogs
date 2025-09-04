package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        batchType = VersioningBatch.class
)
public interface Versioning {

    @ServiceId(pattern = ServiceId.KEBAB_CASE)
    @NonNull
    String getVersioningId();

    @NonNull
    String getVersioningName();

    @NonNull
    String getVersioningModuleId();

    @NonNull
    Validator<String> getVersioningArgValidator();

    @Nullable
    Predicate<CharSequence> getVersioningPredicateOrNull(@Nullable String arg) throws IllegalArgumentException;

    Predicate<CharSequence> NO_VERSIONING_FILTER = null;
}
