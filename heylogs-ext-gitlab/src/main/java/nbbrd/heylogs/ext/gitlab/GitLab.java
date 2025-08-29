package nbbrd.heylogs.ext.gitlab;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.heylogs.spi.Forge;
import nbbrd.heylogs.spi.ForgeSupport;
import nbbrd.service.ServiceProvider;

import java.net.URL;
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
            .linkPredicate(GitLab::isGitLabHost)
            .build();

    static boolean isGitLabHost(@NonNull URL expected) {
        return Arrays.asList(expected.getHost().split("\\.", -1)).contains("gitlab");
    }
}
