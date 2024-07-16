package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;

import java.net.URL;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE
)
public interface Forge {

    @ServiceId(pattern = ServiceId.KEBAB_CASE)
    @NonNull
    String getForgeId();

    @NonNull
    String getForgeName();

    boolean isCompareLink(@NonNull CharSequence text);

    @NonNull
    URL getBaseURL(@NonNull CharSequence text);

    @NonNull
    URL getCompareLink(@NonNull URL latest, @NonNull String nextTag);
}
