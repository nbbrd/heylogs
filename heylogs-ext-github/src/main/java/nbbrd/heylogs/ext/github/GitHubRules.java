package nbbrd.heylogs.ext.github;

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
public final class GitHubRules implements RuleBatch {

    @Override
    public @NonNull Stream<Rule> getProviders() {
        return Stream.of(GITHUB_ISSUE_REF, GITHUB_PULL_REQUEST_REF, GITHUB_MENTION_REF, GITHUB_COMMIT_SHA_REF);
    }

    @VisibleForTesting
    static final Rule GITHUB_ISSUE_REF = ForgeRefRuleSupport
            .builder(GitHubIssueLink::parse, GitHubIssueRef::parse)
            .id("github-issue-ref")
            .name("GitHub issue ref")
            .category("forge")
            .linkPredicate((link, forgeId) -> link.getType().equals(GitHubIssueLink.ISSUES_TYPE) && isGitHubHost(link, forgeId))
            .parsableMessage((link, ref) -> String.format(ROOT, "Expecting issue ref %s, found %s", GitHubIssueRef.of(link, GitHubIssueRef.Type.NUMBER), ref))
            .compatibleMessage((link, ref) -> String.format(ROOT, "Expecting issue ref %s, found %s", GitHubIssueRef.of(link, ref.getType()), ref))
            .build();

    @VisibleForTesting
    static final Rule GITHUB_PULL_REQUEST_REF = ForgeRefRuleSupport
            .builder(GitHubIssueLink::parse, GitHubIssueRef::parse)
            .id("github-pull-request-ref")
            .name("GitHub pull request ref")
            .category("forge")
            .linkPredicate((link, forgeId) -> link.getType().equals(GitHubIssueLink.PULL_REQUEST_TYPE) && isGitHubHost(link, forgeId))
            .parsableMessage((link, ref) -> String.format(ROOT, "Expecting pull request ref %s, found %s", GitHubIssueRef.of(link, GitHubIssueRef.Type.NUMBER), ref))
            .compatibleMessage((link, ref) -> String.format(ROOT, "Expecting pull request ref %s, found %s", GitHubIssueRef.of(link, ref.getType()), ref))
            .build();

    @VisibleForTesting
    static final Rule GITHUB_MENTION_REF = ForgeRefRuleSupport
            .builder(GitHubMentionLink::parse, GitHubMentionRef::parse)
            .id("github-mention-ref")
            .name("GitHub mention ref")
            .category("forge")
            .linkPredicate(GitHubRules::isGitHubHost)
            .parsableMessage((link, ref) -> String.format(ROOT, "Expecting mention ref %s, found %s", GitHubMentionRef.of(link), ref))
            .compatibleMessage((link, ref) -> String.format(ROOT, "Expecting mention ref %s, found %s", GitHubMentionRef.of(link), ref))
            .build();

    @VisibleForTesting
    static final Rule GITHUB_COMMIT_SHA_REF = ForgeRefRuleSupport
            .builder(GitHubCommitSHALink::parse, GitHubCommitSHARef::parse)
            .id("github-commit-sha-ref")
            .name("GitHub commit SHA ref")
            .category("forge")
            .linkPredicate(GitHubRules::isGitHubHost)
            .parsableMessage((link, ref) -> String.format(ROOT, "Expecting commit SHA ref %s, found %s", GitHubCommitSHARef.of(link, GitHubCommitSHARef.Type.HASH), ref))
            .compatibleMessage((link, ref) -> String.format(ROOT, "Expecting commit SHA ref %s, found %s", GitHubCommitSHARef.of(link, ref.getType()), ref))
            .build();

    @VisibleForTesting
    static boolean isGitHubHost(@NonNull ForgeLink expected, @Nullable String forgeId) {
        return GitHub.ID.equals(forgeId) || GitHub.isKnownHost(expected);
    }
}
