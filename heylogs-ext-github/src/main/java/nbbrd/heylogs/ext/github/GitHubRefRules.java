package nbbrd.heylogs.ext.github;

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
public final class GitHubRefRules implements RuleBatch {

    @Override
    public @NonNull Stream<Rule> getProviders() {
        return Stream.of(GITHUB_ISSUE_REF, GITHUB_PULL_REQUEST_REF, GITHUB_MENTION_REF, GITHUB_COMMIT_REF);
    }

    @VisibleForTesting
    static final Rule GITHUB_ISSUE_REF = ForgeRefRuleSupport
            .builder()
            .linkParser(GitHubIssueLink::parse)
            .refParser(GitHubIssueRef::parse)
            .refType(ForgeRefType.ISSUE)
            .id("gh-issue-ref")
            .name("GitHub issue ref")
            .moduleId("github")
            .forgeId(GitHub.ID)
            .linkPredicate(GitHub::isGitHubHost)
            .build();

    @VisibleForTesting
    static final Rule GITHUB_PULL_REQUEST_REF = ForgeRefRuleSupport
            .builder()
            .linkParser(GitHubRequestLink::parse)
            .refParser(GitHubRequestRef::parse)
            .refType(ForgeRefType.REQUEST)
            .id("gh-pull-request-ref")
            .name("GitHub pull request ref")
            .moduleId("github")
            .forgeId(GitHub.ID)
            .linkPredicate(GitHub::isGitHubHost)
            .build();

    @VisibleForTesting
    static final Rule GITHUB_MENTION_REF = ForgeRefRuleSupport
            .builder()
            .linkParser(GitHubMentionLink::parse)
            .refParser(GitHubMentionRef::parse)
            .refType(ForgeRefType.MENTION)
            .id("gh-mention-ref")
            .name("GitHub mention ref")
            .moduleId("github")
            .forgeId(GitHub.ID)
            .linkPredicate(GitHub::isGitHubHost)
            .build();

    @VisibleForTesting
    static final Rule GITHUB_COMMIT_REF = ForgeRefRuleSupport
            .builder()
            .linkParser(GitHubCommitLink::parse)
            .refParser(GitHubCommitRef::parse)
            .refType(ForgeRefType.COMMIT)
            .id("gh-commit-ref")
            .name("GitHub commit ref")
            .moduleId("github")
            .forgeId(GitHub.ID)
            .linkPredicate(GitHub::isGitHubHost)
            .build();
}
