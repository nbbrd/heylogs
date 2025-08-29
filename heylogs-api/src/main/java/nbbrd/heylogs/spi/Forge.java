package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.function.Function;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE
)
public interface Forge {

    @ServiceId(pattern = ServiceId.KEBAB_CASE)
    @NonNull
    String getForgeId();

    @NonNull
    String getForgeName();

    @NonNull
    String getForgeModuleId();

    boolean isCompareLink(@NonNull URL url);

    @NonNull
    CompareLink getCompareLink(@NonNull URL url);

    @Nullable
    Function<? super URL, ForgeLink> getLinkParser(@NonNull ForgeRefType type);

    @Nullable
    Function<? super CharSequence, ForgeRef> getRefParser(@NonNull ForgeRefType type);

    boolean isKnownHost(@NonNull URL url);
}
