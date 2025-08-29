package nbbrd.heylogs.ext.forgejo;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.heylogs.spi.Forge;
import nbbrd.heylogs.spi.ForgeSupport;
import nbbrd.service.ServiceProvider;

import java.net.URL;
import java.util.Arrays;

@DirectImpl
@ServiceProvider
public final class Forgejo implements Forge {

    static final String ID = "forgejo";

    @lombok.experimental.Delegate
    private final Forge delegate = ForgeSupport
            .builder()
            .id(ID)
            .moduleId("forgejo")
            .name("Forgejo")
            .compareLinkFactory(ForgejoCompareLink::parse)
            .linkPredicate(Forgejo::isForgejoHost)
            .build();

    static boolean isForgejoHost(@NonNull URL expected) {
        return Arrays.asList(expected.getHost().split("\\.", -1)).contains("codeberg");
    }
}
