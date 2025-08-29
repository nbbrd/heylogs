package nbbrd.heylogs.ext.gitlab;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.spi.ForgeRefRuleSupport;
import nbbrd.heylogs.spi.ForgeRefType;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleBatch;
import nbbrd.service.ServiceProvider;

import java.util.stream.Stream;

@DirectImpl
@ServiceProvider
public final class GitLabRefRules implements RuleBatch {

    @Override
    public @NonNull Stream<Rule> getProviders() {
        return Stream.of(GITLAB_COMMIT_REF, GITLAB_ISSUE_REF, GITLAB_MERGE_REQUEST_REF, GITLAB_MENTION_REF);
    }

    @VisibleForTesting
    static final Rule GITLAB_COMMIT_REF = ForgeRefRuleSupport
            .builder()
            .linkParser(GitLabCommitLink::parse)
            .refParser(GitLabCommitRef::parse)
            .refType(ForgeRefType.COMMIT)
            .id("gl-commit-ref")
            .name("GitLab commit ref")
            .moduleId("gitlab")
            .forgeId(GitLab.ID)
            .linkPredicate(GitLab::isGitLabHost)
            .build();

    @VisibleForTesting
    static final Rule GITLAB_ISSUE_REF = ForgeRefRuleSupport
            .builder()
            .linkParser(GitLabIssueLink::parse)
            .refParser(GitLabIssueRef::parse)
            .refType(ForgeRefType.ISSUE)
            .id("gl-issue-ref")
            .name("GitLab issue ref")
            .moduleId("gitlab")
            .forgeId(GitLab.ID)
            .linkPredicate(GitLab::isGitLabHost)
            .build();

    @VisibleForTesting
    static final Rule GITLAB_MERGE_REQUEST_REF = ForgeRefRuleSupport
            .builder()
            .linkParser(GitLabRequestLink::parse)
            .refParser(GitLabRequestRef::parse)
            .refType(ForgeRefType.REQUEST)
            .id("gl-merge-request-ref")
            .name("GitLab merge request ref")
            .moduleId("gitlab")
            .forgeId(GitLab.ID)
            .linkPredicate(GitLab::isGitLabHost)
            .build();

    @VisibleForTesting
    static final Rule GITLAB_MENTION_REF = ForgeRefRuleSupport
            .builder()
            .linkParser(GitLabMentionLink::parse)
            .refParser(GitLabMentionRef::parse)
            .refType(ForgeRefType.MENTION)
            .id("gl-mention-ref")
            .name("GitLab mention ref")
            .moduleId("gitlab")
            .forgeId(GitLab.ID)
            .linkPredicate(GitLab::isGitLabHost)
            .build();
}
