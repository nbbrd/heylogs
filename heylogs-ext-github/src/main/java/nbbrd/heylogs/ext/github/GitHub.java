package nbbrd.heylogs.ext.github;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.heylogs.spi.Forge;
import nbbrd.heylogs.spi.ForgeSupport;
import nbbrd.service.ServiceProvider;

import java.net.URL;
import java.util.Arrays;

@DirectImpl
@ServiceProvider
public final class GitHub implements Forge {

    static final String ID = "github";

    @lombok.experimental.Delegate
    private final Forge delegate = ForgeSupport
            .builder()
            .id(ID)
            .moduleId("github")
            .name("GitHub")
            .compareLinkFactory(GitHubCompareLink::parse)
            .linkPredicate(GitHub::isGitHubHost)
            .build();

    static boolean isGitHubHost(@NonNull URL expected) {
        return Arrays.asList(expected.getHost().split("\\.", -1)).contains("github");
    }
}
