package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import org.jspecify.annotations.Nullable;

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

    @NonNull
    String getForgeModuleId();

    @Nullable
    CompareLinkParser getCompareLinkParser();

    @Nullable
    ForgeLinkParser getLinkParser(@NonNull ForgeLinkType type);

    @Nullable
    ForgeRefParser getRefParser(@NonNull ForgeLinkType type);

    @Nullable
    ForgeLinkResolver getLinkResolver(@NonNull ForgeLinkType type);

    @Nullable
    MessageFetcher getMessageFetcher(@NonNull ForgeLinkType type);

    boolean isKnownHost(@NonNull URL url);
}
