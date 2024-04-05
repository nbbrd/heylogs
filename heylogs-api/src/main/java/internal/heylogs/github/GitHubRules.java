package internal.heylogs.github;

import internal.heylogs.GitHostLink;
import internal.heylogs.GitHostRef;
import internal.heylogs.GitHostRefRuleSupport;
import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleBatch;
import nbbrd.service.ServiceProvider;

import java.util.stream.Stream;

@ServiceProvider
public final class GitHubRules implements RuleBatch {

    @Override
    public @NonNull Stream<Rule> getProviders() {
        return Stream.of(GITHUB_ISSUE_REF, GITHUB_PULL_REQUEST_REF, GITHUB_MENTION_REF, GITHUB_COMMIT_SHA_REF);
    }

    @VisibleForTesting
    static final Rule GITHUB_ISSUE_REF = GitHostRefRuleSupport
            .builder(GitHubIssueLink::parse, GitHubIssueRef::parse)
            .id("github-issue-ref")
            .name("GitHub issue ref")
            .category("forge")
            .linkPredicate(expected -> isIssue(expected) && isGitHubHost(expected))
            .message((expected, found) -> messageOf("GitHub issue ref", GitHubIssueRef.of(expected, found.getType()), found))
            .build();

    @VisibleForTesting
    static final Rule GITHUB_PULL_REQUEST_REF = GitHostRefRuleSupport
            .builder(GitHubIssueLink::parse, GitHubIssueRef::parse)
            .id("github-pull-request-ref")
            .name("GitHub pull request ref")
            .category("forge")
            .linkPredicate(expected -> isPullRequest(expected) && isGitHubHost(expected))
            .message((expected, found) -> messageOf("GitHub pull request ref", GitHubIssueRef.of(expected, found.getType()), found))
            .build();

    @VisibleForTesting
    static final Rule GITHUB_MENTION_REF = GitHostRefRuleSupport
            .builder(GitHubMentionLink::parse, GitHubMentionRef::parse)
            .id("github-mention-ref")
            .name("GitHub mention ref")
            .category("forge")
            .linkPredicate(GitHubRules::isGitHubHost)
            .message((expected, found) -> messageOf("GitHub mention ref", GitHubMentionRef.of(expected), found))
            .build();

    @VisibleForTesting
    static final Rule GITHUB_COMMIT_SHA_REF = GitHostRefRuleSupport
            .builder(GitHubCommitSHALink::parse, GitHubCommitSHARef::parse)
            .id("github-commit-sha-ref")
            .name("GitHub commit SHA ref")
            .category("forge")
            .linkPredicate(GitHubRules::isGitHubHost)
            .message((expected, found) -> messageOf("GitHub commit SHA ref", GitHubCommitSHARef.of(expected, found.getType()), found))
            .build();

    private static boolean isGitHubHost(GitHostLink expected) {
        return expected.getBase().getHost().equals("github.com");
    }

    private static boolean isIssue(GitHubIssueLink expected) {
        return expected.getType().equals(GitHubIssueLink.ISSUES_TYPE);
    }

    private static boolean isPullRequest(GitHubIssueLink expected) {
        return expected.getType().equals(GitHubIssueLink.PULL_REQUEST_TYPE);
    }

    private static String messageOf(String name, GitHostRef<?> expected, GitHostRef<?> found) {
        return "Expecting " + name + " " + expected + ", found " + found;
    }
}
