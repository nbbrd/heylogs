package nbbrd.heylogs.ext.gitlab;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.spi.*;
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
            .builder(ForgeRefFactory.of(GitLabCommitLink::parse, GitLabCommitRef::parse, GitLabCommitRef::of))
            .refType(ForgeRefType.COMMIT)
            .id("gl-commit-ref")
            .name("GitLab commit ref")
            .moduleId("gitlab")
            .forgeId(GitLab.ID)
            .linkPredicate(GitLab::isKnownHost)
            .build();

    @VisibleForTesting
    static final Rule GITLAB_ISSUE_REF = ForgeRefRuleSupport
            .builder(ForgeRefFactory.of(GitLabIssueLink::parse, GitLabIssueRef::parse, GitLabIssueRef::of))
            .refType(ForgeRefType.ISSUE)
            .id("gl-issue-ref")
            .name("GitLab issue ref")
            .moduleId("gitlab")
            .forgeId(GitLab.ID)
            .linkPredicate(GitLab::isKnownHost)
            .build();

    @VisibleForTesting
    static final Rule GITLAB_MERGE_REQUEST_REF = ForgeRefRuleSupport
            .builder(ForgeRefFactory.of(GitLabRequestLink::parse, GitLabRequestRef::parse, GitLabRequestRef::of))
            .refType(ForgeRefType.REQUEST)
            .id("gl-merge-request-ref")
            .name("GitLab merge request ref")
            .moduleId("gitlab")
            .forgeId(GitLab.ID)
            .linkPredicate(GitLab::isKnownHost)
            .build();

    @VisibleForTesting
    static final Rule GITLAB_MENTION_REF = ForgeRefRuleSupport
            .builder(ForgeRefFactory.of(GitLabMentionLink::parse, GitLabMentionRef::parse, GitLabMentionRef::of))
            .refType(ForgeRefType.MENTION)
            .id("gl-mention-ref")
            .name("GitLab mention ref")
            .moduleId("gitlab")
            .forgeId(GitLab.ID)
            .linkPredicate(GitLab::isKnownHost)
            .build();
}
