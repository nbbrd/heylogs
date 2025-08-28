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
public final class GitHubRefRules implements RuleBatch {

    @Override
    public @NonNull Stream<Rule> getProviders() {
        return Stream.of(GITHUB_ISSUE_REF, GITHUB_PULL_REQUEST_REF, GITHUB_MENTION_REF, GITHUB_COMMIT_REF);
    }

    @VisibleForTesting
    static final Rule GITHUB_ISSUE_REF = ForgeRefRuleSupport
            .builder(GitHubIssueLink::parse, GitHubIssueRef::parse)
            .id("gh-issue-ref")
            .name("GitHub issue ref")
            .moduleId("github")
            .linkPredicate((link, forgeId) -> link.getType().equals(GitHubIssueLink.ISSUES_TYPE) && isGitHubHost(link, forgeId))
            .parsableMessage((link, ref) -> String.format(ROOT, "Expecting issue ref %s, found %s", GitHubIssueRef.of(link, GitHubIssueRef.Type.NUMBER), ref))
            .compatibleMessage((link, ref) -> String.format(ROOT, "Expecting issue ref %s, found %s", GitHubIssueRef.of(link, ref.getType()), ref))
            .build();

    @VisibleForTesting
    static final Rule GITHUB_PULL_REQUEST_REF = ForgeRefRuleSupport
            .builder(GitHubIssueLink::parse, GitHubIssueRef::parse)
            .id("gh-pull-request-ref")
            .name("GitHub pull request ref")
            .moduleId("github")
            .linkPredicate((link, forgeId) -> link.getType().equals(GitHubIssueLink.PULL_REQUEST_TYPE) && isGitHubHost(link, forgeId))
            .parsableMessage((link, ref) -> String.format(ROOT, "Expecting pull request ref %s, found %s", GitHubIssueRef.of(link, GitHubIssueRef.Type.NUMBER), ref))
            .compatibleMessage((link, ref) -> String.format(ROOT, "Expecting pull request ref %s, found %s", GitHubIssueRef.of(link, ref.getType()), ref))
            .build();

    @VisibleForTesting
    static final Rule GITHUB_MENTION_REF = ForgeRefRuleSupport
            .builder(GitHubMentionLink::parse, GitHubMentionRef::parse)
            .id("gh-mention-ref")
            .name("GitHub mention ref")
            .moduleId("github")
            .linkPredicate(GitHubRefRules::isGitHubHost)
            .parsableMessage((link, ref) -> String.format(ROOT, "Expecting mention ref %s, found %s", GitHubMentionRef.of(link), ref))
            .compatibleMessage((link, ref) -> String.format(ROOT, "Expecting mention ref %s, found %s", GitHubMentionRef.of(link), ref))
            .build();

    @VisibleForTesting
    static final Rule GITHUB_COMMIT_REF = ForgeRefRuleSupport
            .builder(GitHubCommitLink::parse, GitHubCommitRef::parse)
            .id("gh-commit-ref")
            .name("GitHub commit ref")
            .moduleId("github")
            .linkPredicate(GitHubRefRules::isGitHubHost)
            .parsableMessage((link, ref) -> String.format(ROOT, "Expecting commit ref %s, found %s", GitHubCommitRef.of(link, GitHubCommitRef.Type.HASH), ref))
            .compatibleMessage((link, ref) -> String.format(ROOT, "Expecting commit ref %s, found %s", GitHubCommitRef.of(link, ref.getType()), ref))
            .build();

    @VisibleForTesting
    static boolean isGitHubHost(@NonNull ForgeLink expected, @Nullable String forgeId) {
        return GitHub.ID.equals(forgeId) || GitHub.isKnownHost(expected);
    }
}
