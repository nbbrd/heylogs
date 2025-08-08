package nbbrd.heylogs.ext.gitlab;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.heylogs.spi.Forge;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.heylogs.spi.ForgeSupport;
import nbbrd.service.ServiceProvider;

import java.util.Arrays;

@DirectImpl
@ServiceProvider
public final class GitLab implements Forge {

    public static final String ID = "gitlab";

    @lombok.experimental.Delegate
    private final Forge delegate = ForgeSupport
            .builder()
            .id(ID)
            .moduleId("gitlab")
            .name("GitLab")
            .compareLinkFactory(GitLabCompareLink::parse)
            .linkPredicate(GitLab::isKnownHost)
            .build();

    static boolean isKnownHost(@NonNull ForgeLink expected) {
        return Arrays.asList(expected.toURL().getHost().split("\\.", -1)).contains("gitlab");
    }
}
