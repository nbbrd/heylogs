package internal.heylogs.github;

import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;
import nbbrd.design.MightBeGenerated;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.Failure;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleBatch;
import nbbrd.io.text.Parser;
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
        GitHubIssueLink expected = Parser.of(GitHubIssueLink::parse).parse(link.getUrl());
        if (isGitHubLink(expected) && expected.getType().equals(GitHubIssueLink.ISSUES_TYPE)) {
            GitHubIssueRef found = Parser.of(GitHubIssueRef::parse).parse(link.getText());
            if (isCompatibleRef(found, expected)) {
                return Failure
                        .builder()
                        .rule(GITHUB_ISSUE_REF)
                        .message("Expecting GitHub issue ref " + expected.getIssueNumber() + ", found " + found.getIssueNumber())
                        .location(link)
                        .build();
            }
        }
        return NO_PROBLEM;
    }

    @VisibleForTesting
    static Failure validateGitHubPullRequestRef(Link link) {
        GitHubIssueLink expected = Parser.of(GitHubIssueLink::parse).parse(link.getUrl());
        if (isGitHubLink(expected) && expected.getType().equals(GitHubIssueLink.PULL_REQUEST_TYPE)) {
            GitHubIssueRef found = Parser.of(GitHubIssueRef::parse).parse(link.getText());
            if (isCompatibleRef(found, expected)) {
                return Failure
                        .builder()
                        .rule(GITHUB_PULL_REQUEST_REF)
                        .message("Expecting GitHub pull request ref " + expected.getIssueNumber() + ", found " + found.getIssueNumber())
                        .location(link)
                        .build();
            }
        }
        return NO_PROBLEM;
    }

    private static boolean isGitHubLink(GitHubIssueLink expected) {
        return expected != null && expected.getHost().equals("github.com");
    }

    private static boolean isCompatibleRef(GitHubIssueRef found, GitHubIssueLink expected) {
        return found != null && !found.isCompatibleWith(expected);
    }

    @MightBeGenerated
    @ServiceProvider
    public static final class Batch implements RuleBatch {

        @Override
        public @NonNull Stream<Rule> getProviders() {
            return Stream.of(GitHubRules.values());
        }
    }
}
