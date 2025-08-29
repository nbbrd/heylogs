package nbbrd.heylogs.ext.gitlab;

import nbbrd.design.DirectImpl;
import nbbrd.heylogs.spi.Forge;
import nbbrd.heylogs.spi.ForgeSupport;
import nbbrd.service.ServiceProvider;

import static nbbrd.heylogs.spi.ForgeRefType.*;

@DirectImpl
@ServiceProvider
public final class GitLab implements Forge {

    @lombok.experimental.Delegate
    private final Forge delegate = ForgeSupport
            .builder()
            .id("gitlab")
            .moduleId("gitlab")
            .name("GitLab")
            .compareLinkFactory(GitLabCompareLink::parse)
            .knownHostPredicate(ForgeSupport.onHostContaining("gitlab"))
            .parser(COMMIT, GitLabCommitLink::parse, GitLabCommitRef::parse)
            .parser(ISSUE, GitLabIssueLink::parse, GitLabIssueRef::parse)
            .parser(REQUEST, GitLabRequestLink::parse, GitLabRequestRef::parse)
            .parser(MENTION, GitLabMentionLink::parse, GitLabMentionRef::parse)
            .build();
}
