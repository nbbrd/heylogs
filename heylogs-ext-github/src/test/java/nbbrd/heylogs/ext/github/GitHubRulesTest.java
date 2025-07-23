package nbbrd.heylogs.ext.github;

import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleIssue;
import org.junit.jupiter.api.Test;
import tests.heylogs.api.Sample;

import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static nbbrd.heylogs.ext.github.GitHubRules.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;
import static tests.heylogs.api.Sample.using;
import static tests.heylogs.spi.RuleAssert.assertRuleCompliance;

public class GitHubRulesTest {

    @Test
    public void testCompliance() {
        assertRuleCompliance(new GitHubRules());
    }

    @Test
    public void testRules() {
        Node sample = Sample.using("/Main.md");
        for (Rule rule : new GitHubRules().getProviders().collect(toList())) {
            assertThat(Nodes.of(Node.class).descendants(sample).map(rule::getRuleIssueOrNull).filter(Objects::nonNull))
                    .isEmpty();
        }

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitHubIssueRef.md")).map(GITHUB_ISSUE_REF::getRuleIssueOrNull).filter(Objects::nonNull))
                .hasSize(1);

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitHubPullRequestRef.md")).map(GITHUB_PULL_REQUEST_REF::getRuleIssueOrNull).filter(Objects::nonNull))
                .hasSize(1);

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitHubMentionRef.md")).map(GITHUB_MENTION_REF::getRuleIssueOrNull).filter(Objects::nonNull))
                .hasSize(1);

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitHubCommitSHARef.md")).map(GITHUB_COMMIT_SHA_REF::getRuleIssueOrNull).filter(Objects::nonNull))
                .hasSize(1);
    }

    @Test
    public void testValidateGitHubIssueRef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(GITHUB_ISSUE_REF::getRuleIssueOrNull)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubIssueRef.md")))
                .map(GITHUB_ISSUE_REF::getRuleIssueOrNull)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting issue ref #172, found #173").line(2).column(1).build(), atIndex(0))
                .hasSize(1);

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubIssueRefPrefix.md")))
                .map(GITHUB_ISSUE_REF::getRuleIssueOrNull)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting issue ref #172, found 172").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateGitHubPullRequestRef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(GITHUB_PULL_REQUEST_REF::getRuleIssueOrNull)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubPullRequestRef.md")))
                .map(GITHUB_PULL_REQUEST_REF::getRuleIssueOrNull)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting pull request ref #172, found #173").line(2).column(1).build(), atIndex(0))
                .hasSize(1);

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubPullRequestRefPrefix.md")))
                .map(GITHUB_PULL_REQUEST_REF::getRuleIssueOrNull)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting pull request ref #172, found 172").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateGitHubMentionRef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(GITHUB_MENTION_REF::getRuleIssueOrNull)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubMentionRef.md")))
                .map(GITHUB_MENTION_REF::getRuleIssueOrNull)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting mention ref @charphi, found @user").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateGitHubCommitSHARef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(GITHUB_COMMIT_SHA_REF::getRuleIssueOrNull)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubCommitSHARef.md")))
                .map(GITHUB_COMMIT_SHA_REF::getRuleIssueOrNull)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting commit SHA ref 862157d, found 0000000").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testIsGitHubHost() {
        assertThat(isGitHubHost(GitHubIssueLink.parse("https://github.com/nbbrd/heylogs/issues/173"))).isTrue();
        assertThat(isGitHubHost(GitHubIssueLink.parse("https://githubgithub.com/nbbrd/heylogs/issues/173"))).isFalse();
        assertThat(isGitHubHost(GitHubIssueLink.parse("https://github.example.com/nbbrd/heylogs/issues/173"))).isTrue();
        assertThat(isGitHubHost(GitHubIssueLink.parse("https://localhost:8080/nbbrd/heylogs/issues/173"))).isFalse();
    }
}
