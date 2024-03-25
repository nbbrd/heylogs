package internal.heylogs.github;

import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.util.ast.Node;
import internal.heylogs.GitHostSupport;
import lombok.NonNull;
import nbbrd.design.MightBeGenerated;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.Failure;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleBatch;
import nbbrd.service.ServiceProvider;

import java.util.stream.Stream;

import static internal.heylogs.RuleSupport.nameToId;

public enum GitHubRules implements Rule {

    GITHUB_ISSUE_REF {
        @Override
        public Failure validate(@NonNull Node node) {
            return node instanceof Link ? validateGitHubIssueRef((Link) node) : NO_PROBLEM;
        }
    },
    GITHUB_PULL_REQUEST_REF {
        @Override
        public Failure validate(@NonNull Node node) {
            return node instanceof Link ? validateGitHubPullRequestRef((Link) node) : NO_PROBLEM;
        }
    },
    GITHUB_MENTION_REF {
        @Override
        public Failure validate(@NonNull Node node) {
            return node instanceof Link ? validateGitHubMentionRef((Link) node) : NO_PROBLEM;
        }
    };

    @Override
    public @NonNull String getId() {
        return nameToId(this);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @VisibleForTesting
    static Failure validateGitHubIssueRef(Link link) {
        return GitHostSupport.validateRef(
                GitHubIssueLink::parse,
                GitHubIssueRef::parse,
                GitHubRules::isExpectedIssue,
                link, GITHUB_ISSUE_REF,
                GitHubRules::getIssueMessage);
    }

    private static boolean isExpectedIssue(GitHubIssueLink expected) {
        return expected.getType().equals(GitHubIssueLink.ISSUES_TYPE) && expected.getHost().equals("github.com");
    }

    private static String getIssueMessage(GitHubIssueLink expected, GitHubIssueRef found) {
        return "Expecting GitHub issue ref " + (found.isShort() ? GitHubIssueRef.shortOf(expected) : GitHubIssueRef.fullOf(expected)) + ", found " + found;
    }

    @VisibleForTesting
    static Failure validateGitHubPullRequestRef(Link link) {
        return GitHostSupport.validateRef(
                GitHubIssueLink::parse,
                GitHubIssueRef::parse,
                GitHubRules::isExpectedPullRequest,
                link, GITHUB_PULL_REQUEST_REF,
                GitHubRules::getPullRequestMessage);
    }

    private static boolean isExpectedPullRequest(GitHubIssueLink expected) {
        return expected.getType().equals(GitHubIssueLink.PULL_REQUEST_TYPE) && expected.getHost().equals("github.com");
    }

    private static String getPullRequestMessage(GitHubIssueLink expected, GitHubIssueRef found) {
        return "Expecting GitHub pull request ref " + (found.isShort() ? GitHubIssueRef.shortOf(expected) : GitHubIssueRef.fullOf(expected)) + ", found " + found;
    }

    @VisibleForTesting
    static Failure validateGitHubMentionRef(Link link) {
        return GitHostSupport.validateRef(
                GitHubMentionLink::parse,
                GitHubMentionRef::parse,
                GitHubRules::isExpectedMention,
                link, GITHUB_MENTION_REF,
                GitHubRules::getMentionMessage);
    }

    private static boolean isExpectedMention(GitHubMentionLink expected) {
        return expected.getHost().equals("github.com");
    }

    private static String getMentionMessage(GitHubMentionLink expected, GitHubMentionRef found) {
        return "Expecting GitHub mention ref " + GitHubMentionRef.of(expected) + ", found " + found;
    }

    @SuppressWarnings("unused")
    @MightBeGenerated
    @ServiceProvider
    public static final class Batch implements RuleBatch {

        @Override
        public @NonNull Stream<Rule> getProviders() {
            return Stream.of(GitHubRules.values());
        }
    }
}
