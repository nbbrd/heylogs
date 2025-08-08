package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE
)
public interface Versioning {

    @ServiceId(pattern = ServiceId.KEBAB_CASE)
    @NonNull String getVersioningId();

    @NonNull String getVersioningName();

    @NonNull String getVersioningModuleId();

    boolean isValidVersion(@NonNull CharSequence text);
}
