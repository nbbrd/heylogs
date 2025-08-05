package nbbrd.heylogs.ext.gitlab;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.heylogs.spi.ForgeRefRuleSupport;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleBatch;
import nbbrd.service.ServiceProvider;
import org.jspecify.annotations.Nullable;

import java.util.stream.Stream;

import static java.util.Locale.ROOT;

@DirectImpl
@ServiceProvider
public final class GitLabRules implements RuleBatch {

    @Override
    public @NonNull Stream<Rule> getProviders() {
        return Stream.of(GITLAB_COMMIT_REF, GITLAB_ISSUE_REF, GITLAB_MERGE_REQUEST_REF, GITLAB_MENTION_REF);
    }

    @VisibleForTesting
    static final Rule GITLAB_COMMIT_REF = ForgeRefRuleSupport
            .builder(GitLabCommitLink::parse, GitLabCommitRef::parse)
            .id("gitlab-commit-ref")
            .name("GitLab commit ref")
            .category("forge")
            .linkPredicate(GitLabRules::isGitLabHost)
            .parsableMessage((link, ref) -> String.format(ROOT, "Expecting commit ref %s, found %s", GitLabCommitRef.of(link, GitLabRefType.SAME_PROJECT), ref))
            .compatibleMessage((link, ref) -> String.format(ROOT, "Expecting commit ref %s, found %s", GitLabCommitRef.of(link, ref.getType()), ref))
            .build();

    @VisibleForTesting
    static final Rule GITLAB_ISSUE_REF = ForgeRefRuleSupport
            .builder(GitLabIssueLink::parse, GitLabIssueRef::parse)
            .id("gitlab-issue-ref")
            .name("GitLab issue ref")
            .category("forge")
            .linkPredicate(GitLabRules::isGitLabHost)
            .parsableMessage((link, ref) -> String.format(ROOT, "Expecting issue ref %s, found %s", GitLabIssueRef.of(link, GitLabRefType.SAME_PROJECT), ref))
            .compatibleMessage((link, ref) -> String.format(ROOT, "Expecting issue ref %s, found %s", GitLabIssueRef.of(link, ref.getType()), ref))
            .build();

    @VisibleForTesting
    static final Rule GITLAB_MERGE_REQUEST_REF = ForgeRefRuleSupport
            .builder(GitLabMergeRequestLink::parse, GitLabMergeRequestRef::parse)
            .id("gitlab-merge-request-ref")
            .name("GitLab merge request ref")
            .category("forge")
            .linkPredicate(GitLabRules::isGitLabHost)
            .parsableMessage((link, ref) -> String.format(ROOT, "Expecting merge request ref %s, found %s", GitLabMergeRequestRef.of(link, GitLabRefType.SAME_PROJECT), ref))
            .compatibleMessage((link, ref) -> String.format(ROOT, "Expecting merge request ref %s, found %s", GitLabMergeRequestRef.of(link, ref.getType()), ref))
            .build();

    @VisibleForTesting
    static final Rule GITLAB_MENTION_REF = ForgeRefRuleSupport
            .builder(GitLabMentionLink::parse, GitLabMentionRef::parse)
            .id("gitlab-mention-ref")
            .name("GitLab mention ref")
            .category("forge")
            .linkPredicate(GitLabRules::isGitLabHost)
            .parsableMessage((link, ref) -> String.format(ROOT, "Expecting mention ref %s, found %s", GitLabMentionRef.of(link), ref))
            .compatibleMessage((link, ref) -> String.format(ROOT, "Expecting mention ref %s, found %s", GitLabMentionRef.of(link), ref))
            .build();

    @VisibleForTesting
    static boolean isGitLabHost(@NonNull ForgeLink expected, @Nullable String forgeId) {
        return GitLab.ID.equals(forgeId) || GitLab.isKnownHost(expected);
    }
}
