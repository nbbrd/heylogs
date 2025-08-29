package nbbrd.heylogs.ext.github;

import nbbrd.design.DirectImpl;
import nbbrd.heylogs.spi.Forge;
import nbbrd.heylogs.spi.ForgeSupport;
import nbbrd.service.ServiceProvider;

import static nbbrd.heylogs.spi.ForgeRefType.*;

@DirectImpl
@ServiceProvider
public final class GitHub implements Forge {

    @lombok.experimental.Delegate
    private final Forge delegate = ForgeSupport
            .builder()
            .id("github")
            .moduleId("github")
            .name("GitHub")
            .compareLinkFactory(GitHubCompareLink::parse)
            .knownHostPredicate(ForgeSupport.onHostContaining("github"))
            .parser(COMMIT, GitHubCommitLink::parse, GitHubCommitRef::parse)
            .parser(ISSUE, GitHubIssueLink::parse, GitHubIssueRef::parse)
            .parser(REQUEST, GitHubRequestLink::parse, GitHubRequestRef::parse)
            .parser(MENTION, GitHubMentionLink::parse, GitHubMentionRef::parse)
            .build();
}
